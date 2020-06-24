import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
public class App {
    private ItemRepository itemRepository;
    private SalesPromotionRepository salesPromotionRepository;

    public App(ItemRepository itemRepository, SalesPromotionRepository salesPromotionRepository) {
        this.itemRepository = itemRepository;
        this.salesPromotionRepository = salesPromotionRepository;
    }

    public String bestCharge(List<String> inputs) {
        //TODO: write code here
        double total = 0;// 总价
        double pro_1_price = 0; // 使用活动1的价格
        double pro_2_price = 0; // 使用活动2的价格

        // 各个输出句子的模版
        final String ITEMTEMPLATE = "%s x %d = %.0f yuan\n";
        final String PRO_1TEMPLATE = "满30减6 yuan，saving %.0f yuan\n";
        final String PRO_2TEMPLATE = "Half price for certain dishes (%s)，saving %.0f yuan\n";
        final String TOTALTEMPLATE =  "Total：%.0f yuan\n";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("============= Order details =============\n");

        List<Item> allItems = itemRepository.findAll();
        List<SalesPromotion> allSalesPromotions = salesPromotionRepository.findAll();

        HashMap<String, Item> map = new HashMap<>();
        allItems.forEach(obj -> {
            map.put(obj.getId(), obj);
        });

        SalesPromotion pro_1 = allSalesPromotions.stream()
                .filter(item -> item.getType().startsWith("BUY"))
                .collect(Collectors.toList()).get(0);
        SalesPromotion pro_2 = allSalesPromotions.stream()
                .filter(item -> item.getType().startsWith("50%"))
                .collect(Collectors.toList()).get(0);


        String dishes = "";// 菜单
        for (String ele : inputs) {
            String[] item = ele.split(" x ");
            String id = item[0];// 商品id
            int amount = Integer.valueOf(item[1]);// 商品数量
            Item item1 = map.get(id); // 商品
            double price = item1.getPrice() * amount; // 商品总价

            stringBuilder.append(String.format(ITEMTEMPLATE,item1.getName(),amount,price));

            total += price;
            if (pro_2.getRelatedItems().contains(id)) {
                if(dishes.length() != 0) {
                    dishes += "，";
                }
                dishes += item1.getName();
                pro_2_price += price / 2;
            } else {
                pro_2_price += price;
            }

            if(total >= 30){
                pro_1_price = total - ((int)(Math.floor(total /  30)))*6;
            }
        }
        stringBuilder.append("-----------------------------------\n");

        double pro_1_discount = total - pro_1_price; // 使用活动1减免的价格
        double pro_2_discount = total - pro_2_price; // 使用活动2减免的价格

        if(pro_1_discount!=0 && pro_2_discount!=0){
            stringBuilder.append("Promotion used:\n");
            if(pro_1_discount>=pro_2_discount){
                stringBuilder.append(String.format(PRO_1TEMPLATE,pro_1_discount));
                total -= pro_1_discount;
            }else{
                stringBuilder.append(String.format(PRO_2TEMPLATE,dishes,pro_2_discount));
                total -= pro_2_discount;
            }
            stringBuilder.append("-----------------------------------\n");
        }
        stringBuilder.append(String.format(TOTALTEMPLATE,total));
        stringBuilder.append("===================================");

        return stringBuilder.toString();
    }
}
