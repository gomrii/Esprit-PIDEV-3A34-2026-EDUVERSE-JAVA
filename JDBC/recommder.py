import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import sys
import json

def executer_ml(hist_json, dispo_json):
    try:
        # 1. Chargement
        df_hist = pd.DataFrame(json.loads(hist_json))
        df_dispo = pd.DataFrame(json.loads(dispo_json))

        if df_hist.empty or df_dispo.empty:
            return json.dumps([])

        # 2. Profil utilisateur
        # On s'assure que les colonnes existent et ne sont pas vides
        df_hist['combined'] = df_hist['category'].fillna('') + " " + df_hist['level'].fillna('')
        profil_utilisateur = " ".join(df_hist['combined'])
        
        # 3. Préparation dispos
        df_dispo['combinaison'] = df_dispo['category'].fillna('') + " " + df_dispo['level'].fillna('')
        
        # 4. TF-IDF
        vectorizer = TfidfVectorizer()
        tous_les_textes = pd.concat([df_dispo['combinaison'], pd.Series([profil_utilisateur])])
        tfidf_matrix = vectorizer.fit_transform(tous_les_textes)
        
        # 5. Similarité
        scores = cosine_similarity(tfidf_matrix[-1], tfidf_matrix[:-1])
        
        # 6. Tri et retour
        df_dispo['score'] = scores[0]
        # On prend les 2 meilleurs scores > 0
        resultats = df_dispo.sort_values(by='score', ascending=False).head(2)
        
        return resultats.to_json(orient='records')

    except Exception as e:
        # Si une erreur arrive, on l'envoie à Java pour la voir dans la console
        return json.dumps([{"error": str(e)}])

if __name__ == "__main__":
    if len(sys.argv) > 2:
        # Python envoie le résultat vers la sortie standard (sys.stdout)
        print(executer_ml(sys.argv[1], sys.argv[2]))