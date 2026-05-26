package com.studdict.mobile;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.KitchenOrder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * UC8 Gap 8 — KitchenScreen: displayOrder(order).
 * Staff-facing screen that polls active orders every 15 seconds.
 */
public class ScreenKitchenOrders extends Activity {

    private LinearLayout ordersContainer;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private static final long REFRESH_INTERVAL_MS = 15_000L;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchOrders();
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen_orders);
        ordersContainer = findViewById(R.id.kitchenOrdersContainer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchOrders();
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void fetchOrders() {
        ApiClient.getApi().getKitchenOrders().enqueue(new Callback<List<KitchenOrder>>() {
            @Override
            public void onResponse(Call<List<KitchenOrder>> call, Response<List<KitchenOrder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    renderOrders(response.body());
                } else {
                    Toast.makeText(ScreenKitchenOrders.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<KitchenOrder>> call, Throwable t) {
                Toast.makeText(ScreenKitchenOrders.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderOrders(List<KitchenOrder> orders) {
        ordersContainer.removeAllViews();

        if (orders.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No active orders.");
            empty.setTextSize(16);
            empty.setPadding(16, 16, 16, 16);
            ordersContainer.addView(empty);
            return;
        }

        for (KitchenOrder order : orders) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.WHITE);
            card.setPadding(32, 32, 32, 32);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 24);
            card.setLayoutParams(params);

            TextView header = new TextView(this);
            header.setText("Order #" + order.getOrderId() + " — " + order.getTableInfo());
            header.setTextSize(16);
            header.setTextColor(Color.BLACK);
            header.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(header);

            TextView time = new TextView(this);
            time.setText("Placed: " + order.getPlacedAt());
            time.setTextSize(13);
            time.setTextColor(Color.GRAY);
            card.addView(time);

            if (order.getItems() != null) {
                for (String itemLine : order.getItems()) {
                    TextView itemView = new TextView(this);
                    itemView.setText("• " + itemLine);
                    itemView.setTextSize(14);
                    card.addView(itemView);
                }
            }

            ordersContainer.addView(card);
        }
    }
}
