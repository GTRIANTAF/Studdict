package com.studdict.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.Order;
import com.studdict.mobile.model.OrderItemRequest;
import com.studdict.mobile.model.OrderRequest;
import com.studdict.mobile.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenOrderSummary extends Activity {

    private List<CartItemDisplay> displayItems = new ArrayList<>();
    private List<OrderItemRequest> orderItems = new ArrayList<>();
    private int tableId;

    private static class CartItemDisplay {
        long menuItemId;
        int quantity;
        String name;
        double price;

        CartItemDisplay(long menuItemId, int quantity, String name, double price) {
            this.menuItemId = menuItemId;
            this.quantity = quantity;
            this.name = name;
            this.price = price;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        tableId = session.getTableId();
        if (tableId == -1) {
            Toast.makeText(this, "Session expired. Please check in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_order_summary);

        ArrayList<String> cartData = getIntent().getStringArrayListExtra("CART_ITEMS");
        if (cartData != null) {
            for (String data : cartData) {
                // Format: "id|qty|name|price"
                String[] parts = data.split("\\|");
                long id = Long.parseLong(parts[0]);
                int qty = Integer.parseInt(parts[1]);
                String name = parts.length > 2 ? parts[2] : "Item #" + id;
                double price = parts.length > 3 ? Double.parseDouble(parts[3]) : 0.0;
                displayItems.add(new CartItemDisplay(id, qty, name, price));
                orderItems.add(new OrderItemRequest(id, qty));
            }
        }

        RecyclerView recyclerCart = findViewById(R.id.recyclerCart);
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerCart.setAdapter(new CartAdapter(displayItems));

        double total = 0;
        for (CartItemDisplay item : displayItems) total += item.price * item.quantity;
        TextView txtTotal = findViewById(R.id.txtOrderTotal);
        txtTotal.setText(String.format("Total: €%.2f", total));

        Button btnSubmit = findViewById(R.id.btnSubmitOrder);
        btnSubmit.setOnClickListener(v -> submitOrder());

        // UC8 Gap 7: cancel button calls backend cancelOrder endpoint
        Button btnCancel = findViewById(R.id.btnCancelOrder);
        btnCancel.setOnClickListener(v -> cancelOrder());
    }

    private void submitOrder() {
        OrderRequest request = new OrderRequest(tableId, orderItems);
        ApiClient.getApi().createOrder(request).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body();
                    // UC8: the table bill is updated/refreshed on the backend, but the
                    // student is only notified of their order total here. The bill screen
                    // is not shown automatically — it is reached separately when the
                    // student goes to checkout (e.g. from "My Bookings").
                    Toast.makeText(ScreenOrderSummary.this,
                            "Order #" + order.getId() + " placed! Total: €" + order.getTotalAmount(),
                            Toast.LENGTH_LONG).show();
                    finish();
                } else if (response.code() == 409) {
                    // UC8: Item Out of Stock → return to cart
                    Toast.makeText(ScreenOrderSummary.this,
                            "One or more items are out of stock. Please update your cart.",
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(ScreenOrderSummary.this, "Order failed: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                // Demo mode: simulate success. As in the online path, only notify the
                // student of their total — the bill screen is not shown until checkout.
                double total = 0;
                for (CartItemDisplay item : displayItems) total += item.price * item.quantity;
                Toast.makeText(ScreenOrderSummary.this,
                        String.format("Offline demo: order placed! Total: €%.2f", total),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    // UC8 Gap 7: cancel calls POST /api/orders/cancel (per sequence diagram)
    private void cancelOrder() {
        ApiClient.getApi().cancelOrder().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Toast.makeText(ScreenOrderSummary.this, "Order cancelled. No charge.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(ScreenOrderSummary.this, "Order cancelled. No charge.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // --- Adapter ---
    private static class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
        private final List<CartItemDisplay> items;

        CartAdapter(List<CartItemDisplay> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CartItemDisplay item = items.get(position);
            holder.txtQty.setText(item.quantity + "x");
            holder.txtName.setText(item.name + String.format("  (€%.2f each)", item.price));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtQty, txtName;
            ViewHolder(View v) {
                super(v);
                txtQty = v.findViewById(R.id.txtCartItemQty);
                txtName = v.findViewById(R.id.txtCartItemName);
            }
        }
    }
}
