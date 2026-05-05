import os
import subprocess
import sys


def speak_with_powershell(text: str) -> int:
    env = os.environ.copy()
    env["ACCESSIBILITY_TEXT"] = text
    command = (
        "Add-Type -AssemblyName System.Speech; "
        "$speaker = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
        "$speaker.Volume = 100; "
        "$speaker.Rate = 0; "
        "$speaker.Speak($env:ACCESSIBILITY_TEXT)"
    )
    process = subprocess.run(
        ["powershell", "-NoProfile", "-Command", command],
        env=env,
        capture_output=True,
        text=True,
    )
    return process.returncode


def main() -> int:
    text = sys.stdin.read().strip()
    if not text and len(sys.argv) > 1:
        text = " ".join(sys.argv[1:]).strip()

    if not text:
        return 0

    try:
        return speak_with_powershell(text)
    except Exception:
        print(text)
        return 0


if __name__ == "__main__":
    raise SystemExit(main())