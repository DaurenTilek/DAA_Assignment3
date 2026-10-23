
import csv
import os
from collections import defaultdict

import matplotlib.pyplot as plt

CSV_FILE = "results/results.csv"
OUTPUT_DIR = "results/plots"

os.makedirs(OUTPUT_DIR, exist_ok=True)

with open(CSV_FILE, newline="", encoding="utf-8-sig") as file:
    rows = list(csv.DictReader(file))

for row in rows:
    for key in ("n", "m", "dsu_ops", "edge_scans", "comparisons", "time_ms"):
        row[key] = float(row[key]) if key == "time_ms" else int(row[key])


groups = defaultdict(list)

for row in rows:
    if row["algorithm"] in ("Kruskal", "Prim"):
        groups[(row["algorithm"], row["graph"])].append(row)

plt.figure(figsize=(9, 6))

for (algorithm, graph_type), data in sorted(groups.items()):
    data.sort(key=lambda r: r["n"])
    key = "comparisons" if algorithm == "Prim" else "dsu_ops"
    plt.plot(
        [r["n"] for r in data],
        [r[key] for r in data],
        marker="o",
        label=f"{algorithm} {graph_type} ({key})"
    )

plt.xlabel("Number of vertices (n)")
plt.ylabel("Operation counter")
plt.title("Operation counts versus graph size")
plt.xscale("log")
plt.yscale("log")
plt.legend()
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig(f"{OUTPUT_DIR}/operations_vs_n.png", dpi=200)
plt.close()


scan_groups = defaultdict(list)

for row in rows:
    if row["algorithm"] in ("Kruskal", "Prim", "Kosaraju", "Kahn"):
        scan_groups[(row["algorithm"], row["graph"])].append(row)

plt.figure(figsize=(9, 6))

for (algorithm, graph_type), data in sorted(scan_groups.items()):
    data.sort(key=lambda r: r["n"])
    plt.plot(
        [r["n"] for r in data],
        [r["edge_scans"] / r["m"] if r["m"] else 0 for r in data],
        marker="o",
        label=f"{algorithm} {graph_type}"
    )

plt.xlabel("Number of vertices (n)")
plt.ylabel("Edge scans / m")
plt.title("Edge scans per input edge")
plt.xscale("log")
plt.yscale("log")
plt.legend()
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig(f"{OUTPUT_DIR}/scans_per_edge.png", dpi=200)
plt.close()


kruskal_groups = defaultdict(list)

for row in rows:
    if row["algorithm"] == "Kruskal":
        kruskal_groups[row["graph"]].append(row)

plt.figure(figsize=(9, 6))

for graph_type, data in sorted(kruskal_groups.items()):
    data.sort(key=lambda r: r["n"])
    plt.plot(
        [r["n"] for r in data],
        [r["edge_scans"] / r["m"] if r["m"] else 0 for r in data],
        marker="o",
        label=graph_type
    )

plt.xlabel("Number of vertices (n)")
plt.ylabel("Fraction of edges examined (edge_scans / m)")
plt.title("Kruskal: fraction of input edges examined")
plt.legend()
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig(f"{OUTPUT_DIR}/kruskal_edge_fraction.png", dpi=200)
plt.close()

print("Created plots:")
for name in sorted(os.listdir(OUTPUT_DIR)):
    if name.endswith(".png"):
        print(os.path.join(OUTPUT_DIR, name))
