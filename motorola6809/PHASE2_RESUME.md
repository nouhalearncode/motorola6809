# Phase 2 - Offsets & Auto-Inc/Dec Implémentés ✅

## 🎯 Ce qui a été fait

**Date**: 8 décembre 2024

### Modifications apportées à `mode.java`:

#### 1. Mise à jour de `parseIndexedMode`
Supporte désormais :
- Offsets 5-bits : `5,X`, `-3,Y`
- Auto-Incrémentation : `,X+`, `,X++`
- Auto-Décrémentation : `,-X`, `,--X`
- Retourne un format structuré : `TYPE:REGISTRE:VALEUR`

#### 2. Mise à jour de `calculateIndexedAddress`
Gère maintenant les **effets de bord** sur les registres :
- **Post-Incrément** : Utilise l'adresse puis incrémente le registre.
- **Pré-Décrément** : Décrémente le registre puis utilise l'adresse.
- **Offsets** : Calcule l'adresse effective (Base + Offset).

#### 3. Ajout de `decode5BitOffset`
- Implémente la logique d'extension de signe pour les offsets 5-bits (pour référence future et validation).

#### 4. Mise à jour LDA/LDB
- Utilise le nouveau format de parsing.
- Appelle `calculateIndexedAddress` avec les bons paramètres.

---

## ✅ Tests effectués

### Programme de test:
```assembly
LDX #$1000
LDA ,X+  ; A=[1000], X->1001
LDB ,-X  ; X->1000, B=[1000]
LDA 5,X  ; A=[1005]
LDB -3,X ; B=[0FFD]
END
```

### Résultat:
- ✅ **Auto-Inc (,X+)** : Adresse 1000 utilisée, X devient 1001.
- ✅ **Auto-Dec (,-X)** : X devient 1000, Adresse 1000 utilisée.
- ✅ **Offset Positif (5,X)** : Adresse 1005 calculée.
- ✅ **Offset Négatif (-3,X)** : Adresse 0FFD calculée.

---

## 🚀 Prochaines étapes (Phase 3)

- **Accumulateurs** : `A,X`, `B,X`, `D,X`
- Nécessite de lire la valeur du registre accumulateur pour l'ajouter à la base.
