#!/usr/bin/env python3
"""
Generator kunci lisensi untuk Sales Funnel Agen Asuransi.

Cara pakai:
    1. Minta "ID perangkat" dari pelanggan (mereka lihat sendiri di layar
       aktivasi saat trial 5 hari sudah habis, ada tombol salin di situ).
    2. Jalankan: python3 generate_license_key.py <ID_PERANGKAT>
    3. Kirim kunci yang muncul ke pelanggan, mereka masukkan di layar aktivasi.

PENTING: nilai SECRET di bawah ini HARUS SAMA PERSIS dengan yang ada di
app/src/main/java/id/jagakeluarga/salesfunnel/license/LicenseManager.kt
(konstanta SECRET). Kalau salah satu diubah, kunci lama tidak akan valid lagi.
"""
import hashlib
import hmac
import sys

# Harus identik dengan SECRET di LicenseManager.kt
SECRET = "Sukses@2026"


def generate_key(device_id: str) -> str:
    normalized = device_id.strip().upper()
    digest = hmac.new(SECRET.encode(), normalized.encode(), hashlib.sha256).hexdigest()
    hex16 = digest.upper()[:16]
    return "-".join(hex16[i:i + 4] for i in range(0, 16, 4))


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Pemakaian: python3 generate_license_key.py <ID_PERANGKAT>")
        sys.exit(1)
    device_id = sys.argv[1]
    key = generate_key(device_id)
    print(f"ID perangkat : {device_id}")
    print(f"Kunci lisensi: {key}")
