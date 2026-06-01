# Dünya Kupası 2026 — İlkeler ve Kurallar (Taslak)

**Durum:** Uygulamaya geçmeden önce arkadaş grubunun incelemesi için.  
**Sürüm:** Taslak 1.0 · Haziran 2026

---

## Temel ilkeler

1. **Eğlence öncelikli** — Bu, arkadaşlar arası özel bir tahmin oyunudur. Para yok, giriş ücreti yok; ödül sadece gurur.

2. **Adil ve sade** — Kurallar bir dakikada anlatılabilmeli. Puanlamak için Excel gerektiren kuralı kullanmayız.

3. **Beceri + şans** — Puanların çoğu **maç maç** tahminlerden gelir; böylece herkes finale kadar yarışta kalır. Tek seferlik büyük tahminler (ör. şampiyon) sınırlanır; tabloyu domine etmesinler.

4. **Başlama vuruşunda kilit** — Tahmin, maç başladığında kesinleşir. Başlama saati **UTC** olarak saklanır; her oyuncu profilinde kendi saat dilimini görür. Başladıktan sonra düzenleme yok.

5. **Sonucu admin girer** — Resmî skorları sadece adminler girer. Puanlar admin skorlarına göre hesaplanır (canlı API yok — basit ve güvenilir).

6. **Aşamalı açılım** — Önce grup aşaması (bilinen takımlar ve saatler). Eleme ve bracket ekleri, takımlar netleşince açılır.

7. **Şeffaflık** — Sıralama, kurallar ve maç başına puanlar herkese açık. Anlaşmazlıklar grupta çözülür, uygulamada değil.

8. **Eksi puan yok** — Yanlış tahmin için puan düşülmez. En düşük puan sıfırdır.

---

## Önerilen puanlama — maç tahminleri (grup + eleme)

Aksi yazılmadıkça her maç için aynı kurallar.

| Durum | Puan | Açıklama |
|--------|------|----------|
| **Tam skor** (ör. tahmin 2–1, sonuç 2–1) | **5** | En yüksek ödül |
| **Doğru sonuç** (galibiyet / beraberlik / mağlubiyet), skor yanlış | **2** | örn. tahmin 2–1, sonuç 3–1 |
| **Doğru gol averajı** (tam skor değil) | **+1 bonus** | örn. tahmin 3–1 (+2), sonuç 2–0 (+2). Tam skorla birleşmez |
| **Yanlış sonuç** | **0** | |

**Maç başına üst sınır (öneri):** 6 puan (5 tam skor veya 2 + 1 averaj bonusu).

### Eleme turu çarpanı (isteğe bağlı)

Geç turların daha değerli olması için maç puanını tura göre çarp:

| Tur | Çarpan |
|-----|--------|
| Son 32 | ×1,0 |
| Son 16 | ×1,25 |
| Çeyrek final | ×1,5 |
| Yarı final | ×1,75 |
| Üçüncülük | ×1,5 |
| Final | ×2,0 |

Çarpımdan sonra en yakın tam sayıya yuvarla.

---

## Önerilen puanlama — ekstralar (isteğe bağlı, 2. aşama+)

O grubun ilk maçından önce (veya turnuva açılışından önce — grup karar verir) kilitlenir.

| Tahmin | Puan | Kilit |
|--------|------|--------|
| **Grup birincisi** | Grup başına 3 | O grubun ilk maçından önce |
| **Grup ikincisi** | Grup başına 2 | Aynı |
| **Eleme maçı galibi** (skor değil) | 3 | Başlama vuruşu |
| **Yarı finale çıkma** (Son 32 öncesi) | 5 | Son 32 öncesi |
| **Finale çıkma** | 8 | Son 32 öncesi |
| **Turnuva şampiyonu** | 10–12 | Açılış maçından önce |

“En iyi üçüncüler” tahminini, grup özellikle istemedikçe önermiyoruz (karmaşık).

---

## Beraberlik kırıcıları (toplam puan eşitse)

1. En çok **tam skor**  
2. En çok **doğru sonuç** (G/B/M)  
3. **Final** maçına en yakın tahmin (önce tam skor, sonra averaj, sonra tek takım golü)  
4. Yazı tura veya ortak karar

---

## Oyuncu sorumlulukları

- Tahminleri **başlama vuruşundan önce** gir ve güncelle  
- Profilde **saat dilimini** ayarla  
- Hesap paylaşma  
- Puanlama için admin skorlarını kabul et

## Admin sorumlulukları

- Maçlar bitince skorları zamanında gir  
- FIFA düzeltmesi yoksa sonucu değiştirme  
- Knockout / bracket tahminlerini sadece takımlar belli olunca aç

---

## Şimdilik kapsam dışı

- Para veya ödül  
- Canlı bahis / oran entegrasyonu  
- Golcü, kart, VAR tahminleri  
- Otomatik FIFA veri akışı  

---

## Grubun cevaplaması gereken sorular

Koda geçmeden önce lütfen yorumlayın:

- [ ] Eleme turu çarpanı kullanılsın mı?  
- [ ] Grup birincisi / ikincisi tahminleri olsun mu?  
- [ ] Şampiyon tahmini: 10 mu 12 puan mı?  
- [ ] Günlük “tur lideri” +1 bonus olsun mu?  
- [ ] Ön turnuva tahminleri: tek kilit (açılış) mi, grup grup mı?  

---

*Onaylandıktan sonra bu kurallar `PointsServiceImpl` içinde uygulanacak ve uygulamada bir “Kurallar” sayfasında gösterilecek.*
