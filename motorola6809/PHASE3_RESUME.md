# Phase 3 - Offsets Accumulateurs Implément

és ✅

## 🎯 Ce qui a été fait

**Date**: 8 décembre 2024

### Modifications apportées à `mode.java`:

#### 1. Mise à jour de `parseIndexedMode`
Ajout du pattern **Offset Accumulateur** :
- Regex `^[ABD],[XYUS]$` pour détecter `A,X`, `B,Y`, `D,U`, etc.
- Retourne `"ACC_OFFSET:A:X"` format structuré

#### 2. Nouvelle méthode `calculateAccumulatorIndexed`
Gère les **valeurs signées** pour A et B :
- **A et B** : 8-bits signés (-128 à +127)
  - Si valeur > 127 → Convertir en négatif (val - 256)
- **D** : 16-bits non signé (0 à 65535)

#### 3. Mise à jour LDA/LDB
- Détection du type `ACC_OFFSET`
- Appel de `calculateAccumulatorIndexed(acc, indexReg)`
- Gestion unifiée avec les autres modes indexés

---

## ✅ Tests effectués

### Programme de test:
```assembly
LDA #$05
LDX #$1000
LDB A,X    ; A=5, X=1000 => Adresse=1005

LDA #$FE   ; FE = -2 en signé
LDX #$2000
LDB A,X    ; A=-2, X=2000 => Adresse=1FFE

LDA #$12
LDB #$34
LDX #$3000
LDA D,X    ; D=1234, X=3000 => Adresse=4234
END
```

### Résultat:
- ✅ **A positif** : 1000 + 5 = 1005
- ✅ **A négatif** : 2000 - 2 = 1FFE
- ✅ **D 16-bits** : 3000 + 4660 = 4234

---

## 🚀 Prochaines étapes (Phase 4)

- **Offsets 7-bits** : `$7F,X`
- **Offsets 15-bits** : `$7FFF,X`
