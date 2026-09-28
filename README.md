# Cash Power EDG — Organisation du projet

## Répartition des responsabilités

| Membre | Branche | Responsabilité |
|---|---|---|
| Mamadou Ramadane Condé | `feature/navigation` | Socle du projet, navigation, Git/GitHub, intégration finale |
| Ismail | `feature/room-data` | Room, entités, DAO, base de données, couche data |
| Kadija | `feature/ui` | Interface Jetpack Compose et expérience utilisateur |
| R'as Aghul | `feature/viewmodel` | ViewModels, Repository/liaison métier, StateFlow et logique applicative |

## Architecture cible

```text
UI (Compose)
    ↓
ViewModel
    ↓
Repository
    ↓
DAO
    ↓
Room / SQLite
```

La navigation commune est déjà présente dans `feature/navigation`.

## Règle essentielle

Chaque membre travaille uniquement sur sa branche :

- Ismail → `feature/room-data`
- Kadija → `feature/ui`
- R'as Aghul → `feature/viewmodel`

Ne pas modifier directement `main` ni `feature/navigation`.

## Workflow

Avant de travailler :

```bash
git fetch origin
git checkout <ta-branche>
git pull --rebase origin <ta-branche>
```

Après le travail :

```bash
git status
git add .
git commit -m "feat: description du travail"
git push -u origin <ta-branche>
```

Ensuite, ouvrir une Pull Request vers `feature/navigation`, pas directement vers `main`.

## Important

Ne pas inclure :
- mots de passe
- tokens
- clés API privées
- fichiers locaux sensibles
- APK générés inutilement
- modifications hors périmètre sans accord

L'objectif est que chaque branche reste ciblée et facilement intégrable.
