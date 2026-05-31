# Localisation temps réel via GPS et Google Maps

> Application Android développée en Java permettant de récupérer la position GPS d’un utilisateur, l’envoyer vers un backend PHP/MySQL, puis afficher les positions enregistrées sous forme de marqueurs sur Google Maps.

![Platform](https://img.shields.io/badge/platform-Android-green)
![Language](https://img.shields.io/badge/language-Java-orange)
![Backend](https://img.shields.io/badge/backend-PHP-blue)
![Database](https://img.shields.io/badge/database-MySQL-yellow)
![API](https://img.shields.io/badge/API-Volley-purple)
![Maps](https://img.shields.io/badge/maps-Google%20Maps-red)

## Objectif du lab

Ce lab a pour objectif de construire une application Android complète qui combine :

* la récupération de la position GPS ;
* la gestion des permissions de localisation ;
* l’envoi des coordonnées vers un serveur distant ;
* le stockage des positions dans une base MySQL ;
* la récupération des positions en JSON ;
* l’affichage des positions sur Google Maps sous forme de markers.

## demo video 



https://github.com/user-attachments/assets/f3902fde-c9b7-43df-8f38-8ba8b2d36e7e



<img width="921" height="526" alt="image" src="https://github.com/user-attachments/assets/293b859b-ea8a-4e7a-a16d-9277abb43f0c" />


## Fonctionnalités

| Fonctionnalité     | Description                                                       |
| ------------------ | ----------------------------------------------------------------- |
| Localisation GPS   | Récupère latitude et longitude avec `LocationManager`             |
| Permission runtime | Demande `ACCESS_FINE_LOCATION` au lancement                       |
| Envoi serveur      | Envoie latitude, longitude, date et identifiant appareil vers PHP |
| Stockage MySQL     | Insère chaque position dans la table `position`                   |
| API JSON           | Retourne les positions sous forme JSON                            |
| Google Maps        | Affiche les positions enregistrées sous forme de markers          |
| Volley             | Utilisé pour les requêtes HTTP Android                            |
| Cleartext HTTP     | Autorisé pour le backend local en `http://`                       |

## Architecture générale

```text
Localisation/
│
├── app/                         # Application Android Java
│
└── backend-localisation/         # Backend PHP/MySQL, optionnel dans le repo
    │
    ├── classe/
    │   └── Position.php
    │
    ├── connexion/
    │   └── Connexion.php
    │
    ├── dao/
    │   └── IDao.php
    │
    ├── service/
    │   └── PositionService.php
    │
    ├── createPosition.php
    ├── showPositions.php
    └── database.sql
```

## Base de données

Nom de la base :

```sql
localisation
```

Table utilisée :

```sql
CREATE TABLE `position` (
  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `date` datetime NOT NULL,
  `imei` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## Backend PHP

Le backend doit être placé dans le dossier web de XAMPP :

```text
C:\xampp\htdocs\localisation
```

### Endpoints disponibles

| Fichier              | Méthode  | Rôle                                   |
| -------------------- | -------- | -------------------------------------- |
| `createPosition.php` | POST     | Insérer une position dans MySQL        |
| `showPositions.php`  | GET/POST | Retourner toutes les positions en JSON |

### Exemple de réponse `createPosition.php`

```json
{
  "ok": true,
  "message": "Position inserted",
  "received": {
    "latitude": "33.5731",
    "longitude": "-7.5898",
    "date": "2026-05-31 19:00:00",
    "imei": "TEST_DEVICE"
  },
  "ip": "::1"
}
```

### Exemple de réponse `showPositions.php`

```json
{
  "positions": [
    {
      "id": "1",
      "latitude": "33.5731",
      "longitude": "-7.5898",
      "date": "2026-05-31 19:00:00",
      "imei": "TEST_DEVICE"
    }
  ]
}
```

## Tests backend avec PowerShell

Tester l’affichage des positions :

```powershell
Invoke-RestMethod -Uri "http://localhost/localisation/showPositions.php" -Method GET
```

Tester l’insertion d’une position :

```powershell
$body = @{
    latitude = "33.5731"
    longitude = "-7.5898"
    date = "2026-05-31 19:00:00"
    imei = "TEST_DEVICE_POWERSHELL"
}

Invoke-RestMethod `
    -Uri "http://localhost/localisation/createPosition.php" `
    -Method POST `
    -Body $body `
    -ContentType "application/x-www-form-urlencoded"
```

Vérifier dans phpMyAdmin :

```sql
USE localisation;
SELECT * FROM `position` ORDER BY id DESC;
```

## Application Android

Package utilisé :

```text
com.example.localisation
```

Fichiers principaux :

```text
app/src/main/java/com/example/localisation/
│
├── MainActivity.java
└── MapsActivity.java
```

Layouts :

```text
app/src/main/res/layout/
│
├── activity_main.xml
└── activity_maps.xml
```

## Permissions Android

Dans `AndroidManifest.xml` :

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Comme le backend local utilise HTTP, il faut aussi autoriser le trafic clair :

```xml
android:usesCleartextTraffic="true"
```

## Dépendances Gradle

```kotlin
implementation("com.android.volley:volley:1.2.1")
implementation("com.google.android.gms:play-services-maps:18.2.0")
```

## Configuration des URLs

### Si l’application est testée sur émulateur Android Studio

```java
private final String insertUrl = "http://10.0.2.2/localisation/createPosition.php";
private final String showUrl = "http://10.0.2.2/localisation/showPositions.php";
```

### Si l’application est testée sur téléphone réel

Utiliser l’adresse IP du PC serveur XAMPP :

```java
private final String insertUrl = "http://192.168.100.168/localisation/createPosition.php";
private final String showUrl = "http://192.168.100.168/localisation/showPositions.php";
```

Le téléphone et le PC doivent être connectés au même réseau Wi-Fi ou hotspot.

## Google Maps API

Le fichier suivant contient la clé Google Maps :

```text
app/src/main/res/values/strings.xml
```

ou selon le template :

```text
app/src/main/res/values/google_maps_api.xml
```

Exemple :

```xml
<string name="google_maps_key" translatable="false">
    YOUR_GOOGLE_MAPS_API_KEY
</string>
```

Aucune vraie clé API ne doit être publiée dans GitHub.

## Scénario de fonctionnement

```text
1. L’utilisateur lance l’application.
2. L’application demande la permission de localisation.
3. L’utilisateur active le GPS/localisation.
4. L’application récupère latitude et longitude.
5. Les coordonnées sont envoyées au backend PHP avec Volley.
6. Le backend insère la position dans MySQL.
7. L’utilisateur clique sur Afficher Map.
8. MapsActivity appelle showPositions.php.
9. Les positions JSON sont transformées en markers.
10. Les markers sont affichés sur Google Maps.
```

## Tests réalisés

| Test                      | Résultat attendu                         |
| ------------------------- | ---------------------------------------- |
| Test `showPositions.php`  | Retourne `{"positions":[]}` ou une liste |
| Test `createPosition.php` | Retourne `ok=true`                       |
| Vérification MySQL        | Une ligne apparaît dans `position`       |
| Lancement Android         | L’application demande la permission GPS  |
| Position reçue            | Latitude et longitude s’affichent        |
| Envoi Android             | Toast indiquant insertion réussie        |
| Afficher Map              | La carte s’ouvre                         |
| Markers                   | Les positions stockées sont affichées    |

## Problèmes rencontrés et corrections

### 1. `Missing params`

Cause : les champs `latitude`, `longitude`, `date` ou `imei` ne sont pas reçus par PHP.

Correction : vérifier les noms exacts des paramètres envoyés.

### 2. Erreur SQL `1064`

Cause : le nom de table `position` peut poser problème dans la requête SQL.

Correction : utiliser des backticks :

```sql
INSERT INTO `position` (`latitude`, `longitude`, `date`, `imei`)
VALUES (?, ?, ?, ?)
```

### 3. `Volley NoConnectionError`

Cause : URL inaccessible depuis le téléphone ou l’émulateur.

Correction :

* émulateur : utiliser `10.0.2.2` ;
* téléphone réel : utiliser l’IP du PC ;
* vérifier Apache, MySQL, pare-feu Windows et réseau.

### 4. Carte blanche Google Maps

Cause : clé Google Maps absente ou invalide.

Correction : configurer une clé API Google Maps valide avec Maps SDK for Android activé.

## Captures recommandées

Créer un dossier :

```text
docs/screenshots/
```

Captures à ajouter :

```text
docs/screenshots/01_database_position.png
docs/screenshots/02_backend_show_positions.png
docs/screenshots/03_powershell_insert_success.png
docs/screenshots/04_android_permission.png
docs/screenshots/05_android_position_sent.png
docs/screenshots/06_phpmyadmin_position_inserted.png
docs/screenshots/07_google_map_markers.png
```

Exemple d’intégration :

```md
![Table position](docs/screenshots/01_database_position.png)
![API showPositions](docs/screenshots/02_backend_show_positions.png)
![Insertion PowerShell](docs/screenshots/03_powershell_insert_success.png)
![Permission Android](docs/screenshots/04_android_permission.png)
![Position envoyée](docs/screenshots/05_android_position_sent.png)
![Position insérée](docs/screenshots/06_phpmyadmin_position_inserted.png)
![Markers Google Maps](docs/screenshots/07_google_map_markers.png)
```

## Commandes Git utilisées

```bash
git init
git remote add origin https://github.com/bgoussama/-Localisation-temps-r-el-via-GPS-et-Google-Maps.git
git add .
git commit -m "Add Localisation GPS and Google Maps lab"
git branch -M main
git push -u origin main
```

Si le remote existe déjà :

```bash
git remote set-url origin https://github.com/bgoussama/-Localisation-temps-r-el-via-GPS-et-Google-Maps.git
```

## Conclusion

Ce lab permet de comprendre une architecture complète Android + PHP + MySQL autour de la géolocalisation.

L’application récupère la position GPS, l’envoie vers un backend PHP avec Volley, stocke les données dans MySQL, puis affiche les positions enregistrées sur Google Maps sous forme de markers.

Le lab montre aussi les problèmes fréquents liés aux permissions, aux URLs locales, au réseau, au pare-feu Windows, au format des paramètres POST et à la clé Google Maps API.
