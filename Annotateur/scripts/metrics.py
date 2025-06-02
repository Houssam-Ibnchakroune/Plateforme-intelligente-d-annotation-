import sys, csv, json
from collections import defaultdict, Counter
from itertools      import combinations

csv_path = sys.argv[1]

# id_pair -> {annotateur: label}
matrix = defaultdict(dict)
for row in csv.DictReader(open(csv_path, encoding='utf-8')):
    matrix[row['id']][row['annotateur']] = row['classe']

annotateurs = sorted({a for votes in matrix.values() for a in votes})
labels      = sorted({lab for votes in matrix.values() for lab in votes.values()})

# Fleiss κ  ---------------------------------------------------------------
def fleiss_kappa():
    N = len(matrix)                       # items
    n = len(annotateurs)                  # juges
    p = {lab:0 for lab in labels}

    PA = 0
    for votes in matrix.values():
        counts = Counter(votes.values())
        for lab,c in counts.items():      # proportion de chaque label
            p[lab] += c
        PA += sum(c*c for c in counts.values()) - n
    PA = PA / (N * n * (n-1))
    for lab in p: p[lab] /= (N*n)
    Pe = sum(v*v for v in p.values())

    return (PA-Pe)/(1-Pe) if (1-Pe) else 0

# Cohen κ par annotateur (contre majoritaire)
def per_annotator():
    out=[]
    for a in annotateurs:
        agree=0; disagree=0
        for votes in matrix.values():
            maj = Counter(votes.values()).most_common(1)[0][0]
            if a in votes:
                if votes[a]==maj: agree+=1
                else: disagree+=1
        total=agree+disagree
        kappa = (agree - (total/len(labels))) / (total - (total/len(labels))) if total else 0
        out.append({"annotateur":a,"kappa":round(kappa,3)})
    return out

json.dump({
    "fleiss": round(fleiss_kappa(),3),
    "per_annotator": per_annotator()
}, sys.stdout)
