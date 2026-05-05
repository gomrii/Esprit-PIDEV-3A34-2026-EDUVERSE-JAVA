import random
import sys
from urllib.parse import quote

EASY_PATTERNS = [
    "Quelle affirmation definit le mieux {theme} ?",
    "Quel element de base faut-il connaitre en {theme} ?",
    "Quel est l objectif principal de {theme} ?",
    "Quelle pratique est recommandee quand on etudie {theme} ?",
    "Quel exemple illustre correctement {theme} ?",
]

MEDIUM_PATTERNS = [
    "Dans un contexte de {theme}, quel choix est le plus pertinent ?",
    "Quelle difference faut-il retenir a propos de {theme} ?",
    "Quel enchainement d actions correspond a une bonne maitrise de {theme} ?",
    "Quel probleme peut apparaitre si {theme} est mal applique ?",
    "Quel resultat attend-on le plus souvent avec {theme} ?",
]

HARD_PATTERNS = [
    "Dans un cas complexe de {theme}, quelle analyse est la plus juste ?",
    "Quelle affirmation avancee sur {theme} est correcte ?",
    "Quel arbitrage est le plus defendable lorsqu on travaille sur {theme} ?",
    "Quelle erreur subtile doit-on eviter avec {theme} ?",
    "Quel scenario montre une vraie maitrise de {theme} ?",
]

CORRECT_SNIPPETS = [
    "La proposition precise et directement liee au theme.",
    "La reponse qui respecte la logique attendue du sujet.",
    "L option la plus coherente avec les notions essentielles.",
    "Le choix qui applique correctement les bonnes pratiques.",
]

WRONG_SNIPPETS = [
    "Une idee trop vague pour etre juste.",
    "Une option plausible mais incomplete.",
    "Une affirmation hors contexte.",
    "Une reponse qui contredit le principe attendu.",
    "Un choix insuffisant pour traiter le sujet.",
    "Une proposition simpliste et peu fiable.",
]


def select_patterns(level: str):
    normalized = (level or "").strip().lower()
    if normalized == "facile":
        return EASY_PATTERNS
    if normalized == "difficile":
        return HARD_PATTERNS
    return MEDIUM_PATTERNS


def build_question(theme: str, level: str, index: int, duration: int) -> tuple[str, list[tuple[str, float]]]:
    patterns = select_patterns(level)
    pattern = patterns[index % len(patterns)]
    question = pattern.format(theme=theme)
    detail = f"Question {index + 1} sur {theme} pour un quiz de {duration} minutes."
    full_question = f"{question} {detail}"

    correct = f"{theme}: {CORRECT_SNIPPETS[index % len(CORRECT_SNIPPETS)]}"
    wrong_pool = WRONG_SNIPPETS[index % len(WRONG_SNIPPETS):] + WRONG_SNIPPETS[:index % len(WRONG_SNIPPETS)]
    answers = [
        (correct, 1.0),
        (f"{theme}: {wrong_pool[0]}", 0.0),
        (f"{theme}: {wrong_pool[1]}", 0.0),
        (f"{theme}: {wrong_pool[2]}", 0.0),
    ]
    random.shuffle(answers)
    return full_question, answers


def emit_question(question: str, answers: list[tuple[str, float]]):
    print(f"QUESTION|{quote(question)}")
    for answer_text, score in answers:
        print(f"ANSWER|{quote(answer_text)}|{score}")
    print("ENDQUESTION")


def main():
    if len(sys.argv) < 5:
        print("Parametres insuffisants", file=sys.stderr)
        sys.exit(1)

    theme = sys.argv[1].strip()
    duration = int(sys.argv[2])
    level = sys.argv[3].strip() or "moyen"
    count = int(sys.argv[4])

    if not theme:
        print("Le theme est obligatoire", file=sys.stderr)
        sys.exit(1)
    if duration <= 0 or count <= 0:
        print("La duree et le nombre de questions doivent etre positifs", file=sys.stderr)
        sys.exit(1)

    random.seed(f"{theme}|{duration}|{level}|{count}")

    for index in range(count):
        question, answers = build_question(theme, level, index, duration)
        emit_question(question, answers)


if __name__ == "__main__":
    main()
