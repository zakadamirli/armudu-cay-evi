package demo.wehare.project;

import demo.wehare.project.jam.Jam;
import demo.wehare.project.jam.JamRepository;
import demo.wehare.project.menu.Category;
import demo.wehare.project.menu.MenuItem;
import demo.wehare.project.menu.MenuItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Boş bazaya menyunu və mürəbbə rəfini yazır. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final MenuItemRepository menu;
    private final JamRepository jams;

    public DataInitializer(MenuItemRepository menu, JamRepository jams) {
        this.menu = menu;
        this.jams = jams;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (menu.count() == 0) {
            menu.saveAll(List.of(
                    item(Category.CAY, "Lənkəran qara çayı", "Cənub yamaclarının yarpağı. Samovardan, armudu stəkanda.", "2.50", "ən çox", 1),
                    item(Category.CAY, "Kəklikotulu çay", "Dağ kəklikotu ilə dəmlənir. Soyuq axşamların çayı.", "3.00", null, 2),
                    item(Category.CAY, "Zəfəranlı çay", "Bir neçə zəfəran teli, qızılı rəng, yüngül ətir.", "4.50", null, 3),
                    item(Category.CAY, "Qızılgül ləçəkli çay", "Qubanın qızılgülü, qara çayla qarışıq.", "3.50", null, 4),
                    item(Category.SIRIN, "Şəkərbura", "Qoz içli, əl ilə naxışlanmış. Ədədi.", "1.50", null, 5),
                    item(Category.SIRIN, "Bakı paxlavası", "Doqquz qat, fındıq və zəfəran. Bir dilim.", "2.00", null, 6),
                    item(Category.SIRIN, "Mütəkkə", "Kəsilmiş qoz və hil ilə yayma xəmir.", "1.50", null, 7),
                    item(Category.DEST, "Samovar dəsti", "İki nəfərlik samovar, iki mürəbbə, limon, nabat və qənd.", "18.00", "iki nəfər", 8),
                    item(Category.DEST, "Novruz xonçası", "Şəkərbura, paxlava, qoğal, badam, çay. Dörd nəfərlik.", "26.00", "mövsümi", 9),
                    item(Category.DEST, "Nərd və çay", "Nərd taxtası, bir çaynik dəm, bir boşqab quru meyvə.", "9.00", null, 10)
            ));
        }
        if (jams.count() == 0) {
            jams.saveAll(List.of(
                    new Jam("Ağ gilas", "#f0d27a", "Qozla doldurulmuş, şəffaf", "Lənkəran qara çayı ilə. Ağ gilasın şirinliyi tünd dəmi yumşaldır; 5 dəqiqəlik klassik dəm seçin.", 1),
                    new Jam("Qoz", "#5a3418", "Yaşıl qozdan, tünd", "Kəklikotulu çayla. Qozun acımtıl dadı otun ətri ilə yaxşı birləşir; 7 dəqiqəlik tünd dəm.", 2),
                    new Jam("Əncir", "#7d3b5c", "Abşeron ənciri", "Zəfəranlı çayla. Əncirin balı zəfəranın qızılını üstələmir, tamamlayır.", 3),
                    new Jam("Heyva", "#e2a33d", "Dilim-dilim, ətirli", "Qara çayla, limonsuz. Heyvanın turşməzəsi kifayətdir; 3 dəqiqəlik yüngül dəm.", 4),
                    new Jam("Zoğal", "#b0182c", "Turş, al-qırmızı", "Qızılgül ləçəkli çayla. Zoğalın turşluğu qızılgülün şirin ətrini açır.", 5),
                    new Jam("Qızılgül", "#d76b8c", "Ləçək mürəbbəsi", "Sadə Lənkəran çayı ilə. Ləçəklərin ətri çayı öz-özünə bəzəyir, əlavə ot lazım deyil.", 6)
            ));
        }
    }

    private static MenuItem item(Category c, String name, String desc, String price, String tag, int order) {
        return new MenuItem(c, name, desc, new BigDecimal(price), tag, order);
    }
}
