package com.iusjc.weschedule.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Implémentation de Ford-Fulkerson (variante Edmonds-Karp, BFS).
 * Utilisée pour la génération automatique des emplois du temps.
 *
 * Modèle de flot par créneau horaire :
 *   Source
 *     → (UE, Classe)           cap = 1  [une séance par classe par créneau]
 *     → Enseignant_in          cap = 1  [si l'enseignant peut enseigner l'UE]
 *     → Enseignant_out         cap = 1  [anti double-réservation enseignant]
 *     → Salle                  cap = 1  [si capacité ≥ effectif classe]
 *     → Sink                   cap = 1  [anti double-réservation salle]
 */
public class FordFulkerson {

    private final int[][] cap;   // capacités résiduelles
    private final int[][] orig;  // capacités originales (pour détecter flux utilisés)
    private final int n;

    public FordFulkerson(int n) {
        this.n    = n;
        this.cap  = new int[n][n];
        this.orig = new int[n][n];
    }

    /** Ajoute une arête orientée u → v de capacité donnée. */
    public void addEdge(int u, int v, int capacity) {
        cap[u][v]  += capacity;
        orig[u][v] += capacity;
    }

    /**
     * Calcule le flot maximum de source vers sink (Edmonds-Karp).
     * @return valeur du flot maximum
     */
    public int maxFlow(int source, int sink) {
        int totalFlow = 0;
        int[] parent;
        while ((parent = bfs(source, sink)) != null) {
            // Capacité minimale le long du chemin augmentant
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                pathFlow = Math.min(pathFlow, cap[parent[v]][v]);
            }
            // Mise à jour des capacités résiduelles
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                cap[u][v] -= pathFlow;
                cap[v][u] += pathFlow;
            }
            totalFlow += pathFlow;
        }
        return totalFlow;
    }

    /**
     * Indique si du flux a traversé l'arête u → v.
     * Vrai si l'arête existait et que sa capacité résiduelle est inférieure à l'originale.
     */
    public boolean isEdgeUsed(int u, int v) {
        return orig[u][v] > 0 && cap[u][v] < orig[u][v];
    }

    /** BFS pour trouver un chemin augmentant (source → sink). */
    private int[] bfs(int source, int sink) {
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        parent[source] = source;
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v = 0; v < n; v++) {
                if (parent[v] == -1 && cap[u][v] > 0) {
                    parent[v] = u;
                    if (v == sink) return parent;
                    queue.add(v);
                }
            }
        }
        return null;
    }
}
