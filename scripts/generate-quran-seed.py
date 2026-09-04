#!/usr/bin/env python3
"""Generates the Liquibase DML seed files for the immutable Quran reference data
(quran_surah, quran_verse) from two free, public Quran APIs:

  - api.quran.com/api/v4/chapters   surah metadata: Arabic / transliterated / French /
    English names, revelation place, verse count.
  - api.alquran.cloud/v1/quran/quran-uthmani   per-ayah page/juz/hizb-quarter mapping
    for the 604-page Medina Mushaf.

Usage: python3 scripts/generate-quran-seed.py

Writes:
  backend/houndjo-restapi/houndjo-app/src/main/resources/db/changelog/dml/00006-insert-quran-surahs.sql
  backend/houndjo-restapi/houndjo-app/src/main/resources/db/changelog/dml/00007-insert-quran-verses.sql
"""

import json
import urllib.request
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
DML_DIR = REPO_ROOT / "backend/houndjo-restapi/houndjo-app/src/main/resources/db/changelog/dml"

CHAPTERS_URL = "https://api.quran.com/api/v4/chapters?language={lang}"
MUSHAF_URL = "https://api.alquran.cloud/v1/quran/quran-uthmani"

EXPECTED_SURAH_COUNT = 114
EXPECTED_VERSE_COUNT = 6236


def fetch_json(url):
    request = urllib.request.Request(url, headers={"User-Agent": "houndjo-seed-generator/1.0"})
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.load(response)


def sql_escape(value):
    return value.replace("'", "''")


def revelation_place(value):
    return "MECCAN" if value == "makkah" else "MEDINAN"


def generate_surahs():
    chapters_en = {c["id"]: c for c in fetch_json(CHAPTERS_URL.format(lang="en"))["chapters"]}
    chapters_fr = {c["id"]: c for c in fetch_json(CHAPTERS_URL.format(lang="fr"))["chapters"]}

    rows = []
    for number in range(1, EXPECTED_SURAH_COUNT + 1):
        en = chapters_en[number]
        fr = chapters_fr[number]
        rows.append(
            (
                number,
                sql_escape(en["name_arabic"]),
                sql_escape(en["name_simple"]),
                sql_escape(fr["translated_name"]["name"]),
                sql_escape(en["translated_name"]["name"]),
                revelation_place(en["revelation_place"]),
                en["verses_count"],
            )
        )
    return rows


def generate_verses():
    mushaf_surahs = fetch_json(MUSHAF_URL)["data"]["surahs"]
    rows = []
    for surah in mushaf_surahs:
        for ayah in surah["ayahs"]:
            hizb_quarter = ayah["hizbQuarter"]
            hizb = (hizb_quarter + 3) // 4
            rows.append(
                (
                    surah["number"],
                    ayah["numberInSurah"],
                    ayah["page"],
                    ayah["juz"],
                    hizb,
                    hizb_quarter,
                )
            )
    return rows


def write_surahs_sql(rows):
    path = DML_DIR / "00006-insert-quran-surahs.sql"
    values = ",\n".join(
        "    ({}, '{}', '{}', '{}', '{}', '{}', {})".format(*row) for row in rows
    )
    path.write_text(
        "--liquibase formatted sql\n"
        "--changeset houndjo:00006-insert-quran-surahs\n\n"
        "INSERT INTO quran_surah "
        "(number, name_arabic, name_transliteration, name_fr, name_en, revelation_place, verse_count)\n"
        "VALUES\n" + values + ";\n"
    )


def write_verses_sql(rows):
    path = DML_DIR / "00007-insert-quran-verses.sql"
    values = ",\n".join("    ({}, {}, {}, {}, {}, {})".format(*row) for row in rows)
    path.write_text(
        "--liquibase formatted sql\n"
        "--changeset houndjo:00007-insert-quran-verses\n\n"
        "INSERT INTO quran_verse (surah_number, verse_number, page, juz, hizb, hizb_quarter)\n"
        "VALUES\n" + values + ";\n"
    )


def main():
    surah_rows = generate_surahs()
    verse_rows = generate_verses()

    if len(surah_rows) != EXPECTED_SURAH_COUNT:
        raise SystemExit(f"expected {EXPECTED_SURAH_COUNT} surahs, got {len(surah_rows)}")
    if len(verse_rows) != EXPECTED_VERSE_COUNT:
        raise SystemExit(f"expected {EXPECTED_VERSE_COUNT} verses, got {len(verse_rows)}")

    DML_DIR.mkdir(parents=True, exist_ok=True)
    write_surahs_sql(surah_rows)
    write_verses_sql(verse_rows)
    print(f"Wrote {len(surah_rows)} surahs and {len(verse_rows)} verses.")


if __name__ == "__main__":
    main()
