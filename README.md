# Armudu Çay Evi

İçərişəhərdə uydurulmuş bir çay evinin saytı. Spring Boot backend və statik HTML/CSS/JS frontend eyni tətbiqdə işləyir.

- **Menyu:** çay, şirniyyat və dəstlər bazadan gəlir, kateqoriyaya görə süzülür.
- **Dəmləmə saatı:** 3, 5 və ya 7 dəqiqəlik geri sayım. Yalnız brauzerdə işləyir.
- **Mürəbbə rəfi:** kavanoz seçəndə uyğun çay tövsiyəsi göstərilir.
- **Masa rezervi:** yarım saatlıq slotlar, hər slotda qonaq tutumu, server tərəfdə yoxlama.

## Texnologiyalar

Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Validation), H2 (fayl bazası), Gradle.

## İşə salmaq

```bash
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

Sonra brauzerdə http://localhost:8080 ünvanını açın.

`JAVA_HOME` JDK 21 qovluğunu göstərməlidir, məsələn `C:\Program Files\Java\jdk-21`.

Baza `data/armudu.mv.db` faylında saxlanılır və git-ə düşmür. Tətbiq ilk dəfə işə düşəndə menyu və mürəbbələr avtomatik yazılır. Bazanı sıfırlamaq üçün `data/` qovluğunu silin.

## Testlər

```bash
./gradlew test
```

## API

| Metod | Yol | Nə edir |
|---|---|---|
| GET | `/api/menu?category=CAY\|SIRIN\|DEST` | Menyu; `category` verilməsə hamısı |
| GET | `/api/jams` | Mürəbbələr və çay tövsiyələri |
| GET | `/api/reservations/spots` | Oturma yerləri |
| GET | `/api/reservations/slots?date=2026-10-26` | Günün slotları və qalan yer sayı |
| POST | `/api/reservations` | Yeni rezerv |
| GET | `/api/reservations/{id}` | Bir rezerv |
| GET | `/api/reservations` | Bu gündən sonrakı rezervlər |

Rezerv nümunəsi:

```json
{
  "name": "Leyla Məmmədova",
  "phone": "+994 50 123 45 67",
  "date": "2026-10-26",
  "time": "19:00",
  "guests": 2,
  "spot": "EYVAN",
  "note": "Ad günü"
}
```

Cavab kodları: `201` yaradıldı; `400` formada səhv var (`fields` hər sahənin səhvini göstərir); `409` seçilən saatda yer qalmayıb.

Rezerv qaydalarını `src/main/resources/application.properties` faylında dəyişmək olar:

```properties
armudu.slot-capacity=30     # hər yarım saatda ən çox qonaq
armudu.opening-time=10:00
armudu.last-seating=23:30
armudu.max-days-ahead=60
```

## Qovluq quruluşu

```
src/main/java/demo/wehare/project/
  menu/          menyu entity, repository, controller
  jam/           mürəbbə rəfi
  reservation/   rezerv: entity, qaydalar (service), controller
  web/           API səhv cavabları
  DataInitializer.java   ilk məlumatlar
src/main/resources/static/
  index.html, css/style.css, js/app.js
```

> Qeyd: `GET /api/reservations` qonaqların adlarını və qeydlərini qaytarır və hələ heç bir girişlə qorunmur. Saytı internetə çıxarmazdan əvvəl bu endpoint-ə autentifikasiya əlavə edin.
