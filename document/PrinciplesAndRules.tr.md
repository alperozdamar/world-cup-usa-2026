# Dünya Kupası 2026 — İlkeler ve Kurallar (Taslak)

**Durum:** Grup oyunu için onaylı · eleme ekstraları Kurallar sayfasında (puanlama kodu bekliyor).  
**Sürüm:** Taslak 1.1 · Haziran 2026

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

### Eleme turu çarpanı (uygulandı)

Geç turların daha değerli olması için maç puanını tura göre çarp:

| Tur | Çarpan |
|-----|--------|
| Son 32 | ×1,0 |
| Son 16 | ×1,25 |
| Çeyrek final | ×1,5 |
| Yarı final | ×1,75 |
| Üçüncülük | ×1,5 |
| Final | ×2,0 |

Çarpımdan sonra en yakın tam sayıya yuvarla. 90′ skor tabanı ve aşağıdaki eleme ekstralarına uygulanır.

---

## Puanlama — eleme aşaması tahminleri (Kurallar sayfasında; penaltı/ilerleyen puanlaması henüz kodda değil)

Tahminler: `/predictions/knockout`. Sadece **normal süre (90′)** skoru — uzatma dahil değil.

**Nasıl tahmin edilir**
- 90′ tahminin **beraberlik değilse**, önde olan takım otomatik ilerler.
- 90′ tahminin **beraberlikse**, ayrıca **Penaltı atışları?** (Evet/Hayır) ve **kim ilerler** (gerçek maçta uzatma/penaltı sonrası kazanan) seçilir.
- Grup aşaması gibi başlama vuruşunda (UTC) kilitlenir. Her iki takım da netleşince açılır.

**90′ skoru** — grup aşaması ile aynı tablo (5 / 2 / +1 / 0, taban en fazla 6), sonra × tur çarpanı.

**Eleme ekstraları** — yalnızca **gerçek** maç 90′da **berabere** bittiğinde:

| Tahmin | Puan | Not |
|--------|------|-----|
| Doğru **Penaltı atışları?** (Evet / Hayır) | **+1** | Sadece 90′da beraberlik tahmin ettiysen |
| Doğru **ilerleyen takım** | **+2** | Yanlış takım = 0 |

Örnek: 1–1, penaltı Evet, Brezilya ilerler. Gerçek 1–1, penaltı Evet, Brezilya ilerler → 5 + 1 + 2 = 8 taban → çeyrek finalde ×1,5 = **12 puan**. Gerçek skor 2–1 ise sadece 90′ skor tablosu geçerli.

Bracket özeti, güncel grup puan durumundan ve fikstür slot kodlarından (1A, 2F, W73 vb.) takım adlarını gösterir.

---

## Puanlama — grup 1. ve 2. tahminleri (uygulandı)

**O grubun ilk maçından önce** kilitlenir. Tahminler: `/predictions/groups`

| Tahmin | Puan | Kilit |
|--------|------|--------|
| **Doğru 1. sıra** | Grup başına **3** | O grubun ilk maçından önce |
| **Doğru 2. sıra** | Grup başına **2** | Aynı |
| **Doğru takım, yanlış sıra** | **1** (slot başına) | örn. 1. dedin, 2. bitirdi |
| **Yanlış tahmin** | **0** | |

Her iki tahmin de doğruysa grup başına en fazla **5 puan**. Admin, grup aşaması bitince `/admin/group-results` üzerinden resmi sonuçları girer.

---

## Puanlama — final tahmini (şampiyon ve ikinci) (uygulandı)

**Turnuva başlangıcından önce** kilitlenir (ilk grup maçı). Tahminler: `/predictions/final`

| Tahmin | Puan | Kilit |
|--------|------|--------|
| **Doğru şampiyon** | **10** | Turnuva başlangıcından önce |
| **Doğru ikinci** | **5** | Aynı |
| **Finalist, yanlış sıra** | **3** (slot başına) | örn. şampiyon dedin, ikinci bitirdi |
| **Yanlış tahmin** (finalde değil) | **0** | |

Her iki tahmin de doğruysa en fazla **15 puan**. Örnek: final Türkiye (şampiyon) – İspanya (ikinci); sen İspanya şampiyon, Türkiye ikinci dedin → 3 + 3 = 6 puan. Admin final sonrası `/admin/final-result` üzerinden resmi sonucu girer.

---

## Planlanmıyor (şimdilik)

| Tahmin | Gerekçe |
|--------|---------|
| **Yarı finale / finale çıkma** (Son 32 öncesi) | Final tahmini ve maç maç tahminlerle karşılanıyor |
| **En iyi üçüncüler** | Grup istemedikçe fazla karmaşık |

---

## Beraberlik kırıcıları (toplam puan eşitse)

1. En çok **tam skor**  
2. En çok **doğru sonuç** (G/B/M)  
3. **Final** maçına en yakın tahmin (önce tam skor, sonra averaj, sonra tek takım golü)  
4. Yazı tura veya ortak karar

---

## Oyuncu sorumlulukları

- Grup 1./2. tahminlerini **her grubun ilk maçından önce** gir
- Final şampiyon ve ikinci tahminini **turnuva başlamadan önce** gir
- Grup ve eleme maç skoru tahminlerini **başlama vuruşundan önce** gir ve güncelle
- Elemede 90′ beraberlik tahmininde penaltı ve ilerleyen takımı başlamadan önce seç
- Profilde **saat dilimini** ayarla  
- Hesap paylaşma  
- Puanlama için admin skorlarını kabul et

## Admin sorumlulukları

- Maçlar bitince **90′** skorlarını zamanında gir
- Elemede 90′ beraberlikte **penaltı** olup olmadığını ve **kimin ilerlediğini** kaydet
- Grup 1./2. sonuçlarını grup bittikten sonra gir
- Final şampiyon ve ikinciyi final oynandıktan sonra gir  
- FIFA düzeltmesi yoksa sonucu değiştirme  
- Eleme tahminlerini maç takımları netleşince aç

---

## Şimdilik kapsam dışı

- Para veya ödül  
- Canlı bahis / oran entegrasyonu  
- Golcü, kart, VAR tahminleri  
- Otomatik FIFA veri akışı  

---

## Grubun cevaplaması gereken sorular

Koda geçmeden önce lütfen yorumlayın:

- [x] Grup birincisi / ikincisi tahminleri olsun mu? **Evet — uygulandı**
- [x] Final şampiyon / ikinci tahmini (10 / 5 / 3 puan)? **Evet — uygulandı**
- [x] Eleme turu çarpanı kullanılsın mı? **Evet — 90′ skor için uygulandı**
- [x] Eleme penaltı (+1) ve ilerleyen (+2), 90′ beraberlikte? **Evet — Kurallar sayfasında; kod bekliyor**
- [ ] Günlük “tur lideri” +1 bonus olsun mu?  
- [ ] Ön turnuva tahminleri: tek kilit (açılış) mi, grup grup mı?  

---

*Grup ve final puanlaması `PointsServiceImpl` içinde. Eleme 90′ skoru aynı mantık + tur çarpanı. Penaltı ve ilerleyen bonusları Kurallar sayfasında; kod uygulaması bekliyor.*
