import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class Order {
    private String orderId;
    private String status;
    private double totalAmount;
    private datetime placedAt;

    private List<OrderItem> items = new ArrayList<>();
}