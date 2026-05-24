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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenOrderSummary extends Activity {

    private RecyclerView recyclerCart;
    private List<OrderItemRequest> orderItems = new ArrayList<>();
    private int tableId = 1; // Mock Table ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        ArrayList<String> cartData = getIntent().getStringArrayListExtra("CART_ITEMS");
        if (cartData != null) {
            for (String data : cartData) {
                String[] parts = data.split(":");
                orderItems.add(new OrderItemRequest(Long.parseLong(parts[0]), Integer.parseInt(parts[1])));
            }
        }

        recyclerCart = findViewById(R.id.recyclerCart);
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerCart.setAdapter(new CartAdapter(orderItems));

        Button btnSubmit = findViewById(R.id.btnSubmitOrder);
        btnSubmit.setOnClickListener(v -> submitOrder());
    }

    private void submitOrder() {
        OrderRequest request = new OrderRequest(tableId, orderItems);
        ApiClient.getApi().createOrder(request).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body();
                    Toast.makeText(ScreenOrderSummary.this, "Order " + order.getId() + " placed! Total: $" + order.getTotalAmount(), Toast.LENGTH_LONG).show();
                    finish(); // Usually would navigate to tracking or back to home
                } else {
                    Toast.makeText(ScreenOrderSummary.this, "Failed: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Toast.makeText(ScreenOrderSummary.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Adapter ---
    private static class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
        private final List<OrderItemRequest> items;

        CartAdapter(List<OrderItemRequest> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OrderItemRequest item = items.get(position);
            holder.txtQty.setText(item.quantity + "x");
            holder.txtName.setText("Item ID: " + item.menuItemId); // In a real app, map ID to Name
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

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
