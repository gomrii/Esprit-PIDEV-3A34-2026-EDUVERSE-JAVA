import base64
import json
import math
import os
import sys
from collections import deque
from pathlib import Path

try:
    import sounddevice as sd
except ImportError as exc:
    print(
        "Le module Python 'sounddevice' est requis pour la saisie vocale locale. "
        "Installez-le avec 'pip install sounddevice'.",
        file=sys.stderr,
    )
    raise SystemExit(1) from exc

try:
    from vosk import KaldiRecognizer, Model
except ImportError as exc:
    print(
        "Le module Python 'vosk' est requis pour la transcription locale. "
        "Installez-le avec 'pip install vosk'.",
        file=sys.stderr,
    )
    raise SystemExit(1) from exc


SAMPLE_RATE = 16000
CHANNELS = 1
SAMPLE_WIDTH = 2
BLOCK_SIZE = 1600
MAX_RECORDING_SECONDS = 7.0
WAIT_FOR_SPEECH_SECONDS = 3.2
MIN_SPEECH_SECONDS = 1.1
END_SILENCE_SECONDS = 0.9
PREROLL_BLOCKS = 4
SILENCE_RMS_THRESHOLD = 360

_MODEL = None
_MODEL_PATH = None


class SpeechToTextError(Exception):
    pass


def resolve_model_path() -> Path:
    candidates = []

    env_path = os.getenv("VOSK_MODEL_PATH")
    if env_path:
        candidates.append(Path(env_path.strip().strip('"')))

    script_dir = Path(__file__).resolve().parent
    candidates.append(script_dir / "vosk-model")
    candidates.append(script_dir / "vosk-model-small-fr")
    candidates.append(script_dir / "vosk-model-fr")
    candidates.append(Path.cwd() / "vosk-model")

    for candidate in candidates:
        if candidate.is_dir():
            return candidate.resolve()

    if env_path:
        raise SpeechToTextError(
            f"Modele Vosk introuvable. Le dossier configure dans VOSK_MODEL_PATH n'existe pas : {env_path}"
        )

    raise SpeechToTextError(
        "Modele Vosk introuvable. Placez un modele local dans 'scripts/vosk-model' "
        "ou definissez la variable d'environnement VOSK_MODEL_PATH."
    )


def get_model() -> Model:
    global _MODEL, _MODEL_PATH

    model_path = resolve_model_path()
    if _MODEL is not None and _MODEL_PATH == model_path:
        return _MODEL

    try:
        _MODEL = Model(str(model_path))
        _MODEL_PATH = model_path
        return _MODEL
    except Exception as exc:
        raise SpeechToTextError(
            f"Impossible de charger le modele local de transcription depuis '{model_path}' : {exc}"
        ) from exc


def create_recognizer() -> KaldiRecognizer:
    try:
        recognizer = KaldiRecognizer(get_model(), SAMPLE_RATE)
        recognizer.SetWords(False)
        recognizer.SetPartialWords(False)
        return recognizer
    except SpeechToTextError:
        raise
    except Exception as exc:
        raise SpeechToTextError(f"Impossible de preparer Vosk pour la transcription : {exc}") from exc


def compute_rms(data: bytes) -> float:
    sample_count = len(data) // SAMPLE_WIDTH
    if sample_count == 0:
        return 0.0

    samples = memoryview(data).cast("h")
    energy = 0.0
    for sample in samples:
        energy += float(sample) * float(sample)

    return math.sqrt(energy / sample_count)


def block_is_speech(data: bytes) -> bool:
    return compute_rms(data) >= SILENCE_RMS_THRESHOLD


def record_and_transcribe() -> str:
    try:
        sd.check_input_settings(samplerate=SAMPLE_RATE, channels=CHANNELS, dtype="int16")
    except Exception as exc:
        raise SpeechToTextError(f"Micro indisponible ou non configure : {exc}") from exc

    recognizer = create_recognizer()
    preroll = deque(maxlen=PREROLL_BLOCKS)
    speech_started = False
    speech_detected_blocks = 0
    silent_blocks_after_speech = 0
    max_blocks = max(1, int(MAX_RECORDING_SECONDS * SAMPLE_RATE / BLOCK_SIZE))
    wait_blocks = max(1, int(WAIT_FOR_SPEECH_SECONDS * SAMPLE_RATE / BLOCK_SIZE))
    min_speech_blocks = max(1, int(MIN_SPEECH_SECONDS * SAMPLE_RATE / BLOCK_SIZE))
    end_silence_blocks = max(1, int(END_SILENCE_SECONDS * SAMPLE_RATE / BLOCK_SIZE))

    try:
        with sd.RawInputStream(
            samplerate=SAMPLE_RATE,
            blocksize=BLOCK_SIZE,
            channels=CHANNELS,
            dtype="int16",
            latency="low",
        ) as stream:
            for block_index in range(max_blocks):
                data, overflowed = stream.read(BLOCK_SIZE)
                chunk = bytes(data)

                if overflowed:
                    raise SpeechToTextError("Le micro n'a pas pu enregistrer correctement l'audio.")

                speech_now = block_is_speech(chunk)

                if not speech_started:
                    preroll.append(chunk)
                    if speech_now:
                        speech_started = True
                        speech_detected_blocks = 1
                        for buffered_chunk in preroll:
                            recognizer.AcceptWaveform(buffered_chunk)
                        preroll.clear()
                    elif block_index + 1 >= wait_blocks:
                        raise SpeechToTextError(
                            "Aucune parole detectee apres plusieurs secondes. Reessayez en parlant normalement."
                        )
                    continue

                recognizer.AcceptWaveform(chunk)

                if speech_now:
                    speech_detected_blocks += 1
                    silent_blocks_after_speech = 0
                else:
                    silent_blocks_after_speech += 1
                    if (
                        speech_detected_blocks >= min_speech_blocks
                        and silent_blocks_after_speech >= end_silence_blocks
                    ):
                        break
    except SpeechToTextError:
        raise
    except Exception as exc:
        raise SpeechToTextError(f"Impossible d'enregistrer l'audio depuis le micro : {exc}") from exc

    try:
        result = json.loads(recognizer.FinalResult())
    except json.JSONDecodeError as exc:
        raise SpeechToTextError(f"La transcription locale a retourne un format invalide : {exc}") from exc

    transcript = (result.get("text") or "").strip()
    if transcript:
        return transcript

    partial = json.loads(recognizer.PartialResult() or "{}").get("partial", "").strip()
    if partial:
        return partial

    raise SpeechToTextError("Aucun texte n'a ete reconnu. Reessayez en parlant un peu plus distinctement.")


def encode_message(message: str) -> str:
    return base64.b64encode(message.encode("utf-8")).decode("ascii")


def run_single() -> int:
    try:
        print(record_and_transcribe())
        return 0
    except SpeechToTextError as exc:
        print(str(exc), file=sys.stderr)
        return 1
    except Exception as exc:
        print(f"Erreur inattendue pendant la transcription locale : {type(exc).__name__}: {exc}", file=sys.stderr)
        return 1


def run_worker() -> int:
    try:
        model_path = resolve_model_path()
        get_model()
        print(f"READY\t{encode_message(str(model_path))}", flush=True)
    except SpeechToTextError as exc:
        print(str(exc), file=sys.stderr)
        return 1
    except Exception as exc:
        print(f"Erreur inattendue au demarrage du worker local : {type(exc).__name__}: {exc}", file=sys.stderr)
        return 1

    for raw_line in sys.stdin:
        command = raw_line.strip().upper()

        if command == "TRANSCRIBE":
            try:
                transcript = record_and_transcribe()
                print(f"OK\t{encode_message(transcript)}", flush=True)
            except SpeechToTextError as exc:
                print(f"ERR\t{encode_message(str(exc))}", flush=True)
            except Exception as exc:
                message = f"Erreur inattendue pendant la transcription locale : {type(exc).__name__}: {exc}"
                print(f"ERR\t{encode_message(message)}", flush=True)
        elif command == "EXIT":
            return 0
        elif command:
            message = f"Commande inconnue pour le worker de transcription : {command}"
            print(f"ERR\t{encode_message(message)}", flush=True)

    return 0


def main() -> int:
    if "--worker" in sys.argv[1:]:
        return run_worker()
    return run_single()


if __name__ == "__main__":
    raise SystemExit(main())
