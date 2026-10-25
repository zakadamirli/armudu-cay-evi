package az.armudu.cayevi;

import az.armudu.cayevi.i18n.Text;
import az.armudu.cayevi.jam.Jam;
import az.armudu.cayevi.jam.JamRepository;
import az.armudu.cayevi.menu.Category;
import az.armudu.cayevi.menu.MenuItem;
import az.armudu.cayevi.menu.MenuItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Boş bazaya menyunu və mürəbbə rəfini üç dildə (az/en/ru) yazır. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final MenuItemRepository menu;
    private final JamRepository jams;

    public DataInitializer(MenuItemRepository menu, JamRepository jams) {
        this.menu = menu;
        this.jams = jams;
    }

    private static Text t(String az, String en, String ru) {
        return new Text(az, en, ru);
    }

    private static MenuItem item(Category c, Text name, Text desc, Text tag, String price, Integer brew, int order) {
        return new MenuItem(c, name, desc, tag, new BigDecimal(price), brew, order);
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (menu.count() == 0) {
            menu.saveAll(List.of(
                    item(Category.CAY,
                            t("Lənkəran qara çayı", "Lankaran black tea", "Ланкяранский чёрный чай"),
                            t("Cənub yamaclarının yarpağı. Samovardan, armudu stəkanda.",
                              "Leaf from the southern slopes. From the samovar, in an armudu glass.",
                              "Лист с южных склонов. Из самовара, в стакане армуду."),
                            t("ən çox", "most loved", "хит"), "2.50", 5, 1),
                    item(Category.CAY,
                            t("Kəklikotulu çay", "Thyme tea", "Чай с чабрецом"),
                            t("Dağ kəklikotu ilə dəmlənir. Soyuq axşamların çayı.",
                              "Brewed with mountain thyme. A tea for cold evenings.",
                              "Заваривается с горным чабрецом. Чай для холодных вечеров."),
                            null, "3.00", 7, 2),
                    item(Category.CAY,
                            t("Zəfəranlı çay", "Saffron tea", "Чай с шафраном"),
                            t("Bir neçə zəfəran teli, qızılı rəng, yüngül ətir.",
                              "A few saffron threads, golden colour, light aroma.",
                              "Несколько нитей шафрана, золотистый цвет, лёгкий аромат."),
                            null, "4.50", 4, 3),
                    item(Category.CAY,
                            t("Qızılgül ləçəkli çay", "Rose petal tea", "Чай с лепестками роз"),
                            t("Qubanın qızılgülü, qara çayla qarışıq.",
                              "Guba roses blended with black tea.",
                              "Губинская роза в смеси с чёрным чаем."),
                            null, "3.50", 5, 4),
                    item(Category.SIRIN,
                            t("Şəkərbura", "Shakarbura", "Шекербура"),
                            t("Qoz içli, əl ilə naxışlanmış. Ədədi.",
                              "Walnut-filled, hand-patterned. Per piece.",
                              "С начинкой из грецкого ореха, с ручным узором. За штуку."),
                            null, "1.50", null, 5),
                    item(Category.SIRIN,
                            t("Bakı paxlavası", "Baku pakhlava", "Бакинская пахлава"),
                            t("Doqquz qat, fındıq və zəfəran. Bir dilim.",
                              "Nine layers, hazelnut and saffron. One slice.",
                              "Девять слоёв, фундук и шафран. Один кусочек."),
                            null, "2.00", null, 6),
                    item(Category.SIRIN,
                            t("Mütəkkə", "Mutaki", "Мутаки"),
                            t("Kəsilmiş qoz və hil ilə yayma xəmir.",
                              "Rolled pastry with chopped walnut and cardamom.",
                              "Рулетики из теста с рубленым орехом и кардамоном."),
                            null, "1.50", null, 7),
                    item(Category.DEST,
                            t("Samovar dəsti", "Samovar set", "Самоварный набор"),
                            t("İki nəfərlik samovar, iki mürəbbə, limon, nabat və qənd.",
                              "Samovar for two, two jams, lemon, rock sugar and sugar.",
                              "Самовар на двоих, два варенья, лимон, набат и сахар."),
                            t("iki nəfər", "for two", "на двоих"), "18.00", null, 8),
                    item(Category.DEST,
                            t("Novruz xonçası", "Novruz tray", "Новрузный хончя"),
                            t("Şəkərbura, paxlava, qoğal, badam, çay. Dörd nəfərlik.",
                              "Shakarbura, pakhlava, qogal, almonds, tea. For four.",
                              "Шекербура, пахлава, когал, миндаль, чай. На четверых."),
                            t("mövsümi", "seasonal", "сезонное"), "26.00", null, 9),
                    item(Category.DEST,
                            t("Nərd və çay", "Backgammon and tea", "Нарды и чай"),
                            t("Nərd taxtası, bir çaynik dəm, bir boşqab quru meyvə.",
                              "A backgammon board, a pot of tea, a plate of dried fruit.",
                              "Доска для нард, чайник чая, тарелка сухофруктов."),
                            null, "9.00", null, 10)
            ));
        }
        if (jams.count() == 0) {
            jams.saveAll(List.of(
                    new Jam(t("Ağ gilas", "White cherry", "Белая черешня"), "#f0d27a",
                            t("Qozla doldurulmuş, şəffaf", "Stuffed with walnut, translucent", "С орехом внутри, прозрачное"),
                            t("Lənkəran qara çayı ilə. Ağ gilasın şirinliyi tünd dəmi yumşaldır; 5 dəqiqəlik klassik dəm seçin.",
                              "With Lankaran black tea. The cherry sweetness softens a strong brew; pick the 5-minute classic.",
                              "С ланкяранским чёрным чаем. Сладость черешни смягчает крепкую заварку; выберите 5-минутную классику."), 1),
                    new Jam(t("Qoz", "Walnut", "Грецкий орех"), "#5a3418",
                            t("Yaşıl qozdan, tünd", "From green walnuts, dark", "Из зелёного ореха, тёмное"),
                            t("Kəklikotulu çayla. Qozun acımtıl dadı otun ətri ilə yaxşı birləşir; 7 dəqiqəlik tünd dəm.",
                              "With thyme tea. The slightly bitter walnut pairs well with the herb; a 7-minute strong brew.",
                              "С чаем из чабреца. Горьковатый орех хорошо сочетается с травой; крепкая 7-минутная заварка."), 2),
                    new Jam(t("Əncir", "Fig", "Инжир"), "#7d3b5c",
                            t("Abşeron ənciri", "Absheron figs", "Абшеронский инжир"),
                            t("Zəfəranlı çayla. Əncirin balı zəfəranın qızılını üstələmir, tamamlayır.",
                              "With saffron tea. The fig honey does not overpower the saffron, it completes it.",
                              "С шафрановым чаем. Медовость инжира не перебивает шафран, а дополняет его."), 3),
                    new Jam(t("Heyva", "Quince", "Айва"), "#e2a33d",
                            t("Dilim-dilim, ətirli", "Sliced, fragrant", "Дольками, ароматное"),
                            t("Qara çayla, limonsuz. Heyvanın turşməzəsi kifayətdir; 3 dəqiqəlik yüngül dəm.",
                              "With black tea, no lemon. The quince tartness is enough; a 3-minute light brew.",
                              "С чёрным чаем, без лимона. Кислинки айвы достаточно; лёгкая 3-минутная заварка."), 4),
                    new Jam(t("Zoğal", "Cornelian cherry", "Кизил"), "#b0182c",
                            t("Turş, al-qırmızı", "Tart, bright red", "Кислое, ярко-красное"),
                            t("Qızılgül ləçəkli çayla. Zoğalın turşluğu qızılgülün şirin ətrini açır.",
                              "With rose petal tea. The cornelian tartness opens up the sweet rose aroma.",
                              "С чаем из лепестков роз. Кислинка кизила раскрывает сладкий аромат розы."), 5),
                    new Jam(t("Qızılgül", "Rose", "Роза"), "#d76b8c",
                            t("Ləçək mürəbbəsi", "Petal jam", "Варенье из лепестков"),
                            t("Sadə Lənkəran çayı ilə. Ləçəklərin ətri çayı öz-özünə bəzəyir, əlavə ot lazım deyil.",
                              "With plain Lankaran tea. The petals scent the tea on their own, no extra herbs needed.",
                              "С простым ланкяранским чаем. Лепестки сами ароматизируют чай, травы не нужны."), 6)
            ));
        }
    }
}
