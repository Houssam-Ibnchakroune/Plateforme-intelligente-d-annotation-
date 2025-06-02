#!/usr/bin/env python3
"""
Détection simple de spammeurs.

Entrée : CSV  id,text1,text2,classe,annotateur,[...]
Sortie  : JSON  {"spammers":[...], "details":[...]}
"""

import argparse, csv, json, sys, warnings
from collections import Counter, defaultdict
from pathlib import Path

import numpy   as np
from sklearn.metrics import cohen_kappa_score
# ────────────────────────────────────────────────────────────────
#  juste après les imports, AVANT d’appeler scikit-learn
# ────────────────────────────────────────────────────────────────
warnings.filterwarnings("ignore", category=UserWarning)    # déjà présent
warnings.filterwarnings("ignore", category=RuntimeWarning) # ← ajoutez ceci

# si vous préférez être très radical :
# warnings.filterwarnings("ignore")   # ignore TOUS les warnings
# ──────────────────────────────────────────────────────────────
# ------------------------------------------------------------------ #
# 1) arguments CLI
# ------------------------------------------------------------------ #
ap = argparse.ArgumentParser()
ap.add_argument("csv", help="fichier d’annotations")
ap.add_argument("--min_pairs", type=int, default=3,
                help="nb mini de paires pour juger un annotateur")
ap.add_argument("--kappa_thr", type=float, default=0.20,
                help="seuil κ → spammeur")
args = ap.parse_args()

def log(*xs): print(*xs, file=sys.stderr)

# ------------------------------------------------------------------ #
# 2) détection du séparateur
# ------------------------------------------------------------------ #
csv_path = Path(args.csv)
if not csv_path.exists():
    sys.exit(f"[spam] fichier introuvable : {csv_path}")

first_line = csv_path.read_text(encoding="utf-8", errors="ignore").splitlines()[0]
try:
    delim = csv.Sniffer().sniff(first_line).delimiter
except Exception:
    delim = ','

# ------------------------------------------------------------------ #
# 3) lecture + normalisation
# ------------------------------------------------------------------ #
rows = []
with csv_path.open(encoding="utf-8", errors="ignore") as fh:
    rdr = csv.DictReader(fh, delimiter=delim)
    for raw in rdr:
        rid   = raw.get("id")          or raw.get("pair_id")
        user  = raw.get("annotateur")  or raw.get("annotator") or raw.get("user")
        label = raw.get("classe")      or raw.get("label")     or raw.get("class")
        if None in (rid, user, label):
            continue
        rows.append({"id": rid, "annotateur": user, "label": label})

if not rows:
    sys.exit(json.dumps({"spammers": [], "details": []}))

# ------------------------------------------------------------------ #
# 4) matrice  id  → {annotateur: label}
# ------------------------------------------------------------------ #
by_item = defaultdict(dict)
for r in rows:
    by_item[r["id"]][r["annotateur"]] = r["label"]

majority = {pid: Counter(v.values()).most_common(1)[0][0]
            for pid, v in by_item.items()}

# ------------------------------------------------------------------ #
# 5) κ  annotateur vs majorité  (robuste)
# ------------------------------------------------------------------ #
details = []
warnings.filterwarnings("ignore", category=UserWarning)

def safe_kappa(gold, pred) -> float:
    """Cohen κ qui ne renvoie jamais NaN."""
    try:
        k = cohen_kappa_score(gold, pred)
        if np.isnan(k):          # cas « un seul label » → NaN
            return 0.0
        return float(k)
    except ValueError:           # listes vides, ou labels incohérents
        return 0.0

annotateurs = {r["annotateur"] for r in rows}
for ann in annotateurs:
    gold, pred = [], []
    for pid, votes in by_item.items():
        if ann in votes:
            pred.append(votes[ann])
            gold.append(majority[pid])

    n = len(pred)
    if n < args.min_pairs:
        continue

    # encodage labels→int (sklearn exige des entiers)
    labels = sorted({*gold, *pred})
    idx    = {lab: i for i, lab in enumerate(labels)}
    kappa  = safe_kappa([idx[x] for x in gold],
                        [idx[x] for x in pred])

    details.append({"annotateur": ann,
                    "kappa": round(kappa, 3),
                    "n": n})

# ------------------------------------------------------------------ #
# 6) spammers
# ------------------------------------------------------------------ #
spammers = [d["annotateur"] for d in details
            if d["kappa"] < args.kappa_thr]

json.dump({"spammers": spammers, "details": details},
          sys.stdout, ensure_ascii=False)
sys.stdout.write("\n")
