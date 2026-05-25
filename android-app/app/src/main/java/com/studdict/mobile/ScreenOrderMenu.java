package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
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
import com.studdict.mobile.model.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenOrderMenu extends Activity {

    private RecyclerView recyclerMenu;
    private MenuAdapter adapter;
    private Map<Long, Integer> cart = new HashMap<>(); // MenuItem ID to Quantity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_menu);

        recyclerMenu = findViewById(R.id.recyclerMenu);
        recyclerMenu.setLayoutManager(new LinearLayoutManager(this));

        Button btnReviewOrder = findViewById(R.id.btnReviewOrder);
        btnReviewOrder.setOnClickListener(v -> {
            if (cart.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ScreenOrderSummary.class);
            // In a real app we'd pass the cart via intent or shared ViewModel. 
            // For simplicity we pass the cart items.
            ArrayList<String> cartItems = new ArrayList<>();
            for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
                cartItems.add(entry.getKey() + ":" + entry.getValue());
            }
            intent.putStringArrayListExtra("CART_ITEMS", cartItems);
            startActivity(intent);
        });

        fetchCatalog();
    }

    private void fetchCatalog() {
        ApiClient.getApi().readCatalog().enqueue(new Callback<List<MenuItem>>() {
            @Override
            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new MenuAdapter(response.body(), cart);
                    recyclerMenu.setAdapter(adapter);
                } else {
                    Toast.makeText(ScreenOrderMenu.this, "Failed to load catalog", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MenuItem>> call, Throwable t) {
                Toast.makeText(ScreenOrderMenu.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Adapter ---
    private static class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
        private final List<MenuItem> items;
        private final Map<Long, Integer> cart;

        MenuAdapter(List<MenuItem> items, Map<Long, Integer> cart) {
            this.items = items;
            this.cart = cart;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MenuItem item = items.get(position);
            holder.txtName.setText(item.getName());
            holder.txtPrice.setText("$" + String.format("%.2f", item.getPrice()));

            Integer qtyVal = cart.get(item.getItemId());
            int qty = qtyVal != null ? qtyVal : 0;
            holder.txtQty.setText(String.valueOf(qty));

            holder.btnAdd.setOnClickListener(v -> {
                Integer qVal = cart.get(item.getItemId());
                int q = (qVal != null ? qVal : 0) + 1;
                cart.put(item.getItemId(), q);
                notifyItemChanged(position);
            });

            holder.btnRemove.setOnClickListener(v -> {
                Integer qVal = cart.get(item.getItemId());
                int q = qVal != null ? qVal : 0;
                if (q > 0) {
                    q--;
                    if (q == 0) cart.remove(item.getItemId());
                    else cart.put(item.getItemId(), q);
                    notifyItemChanged(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtName, txtPrice, txtQty;
            Button btnAdd, btnRemove;

            ViewHolder(View v) {
                super(v);
                txtName = v.findViewById(R.id.txtMenuName);
                txtPrice = v.findViewById(R.id.txtMenuPrice);
                txtQty = v.findViewById(R.id.txtItemQty);
                btnAdd = v.findViewById(R.id.btnAddItem);
                btnRemove = v.findViewById(R.id.btnRemoveItem);
            }
        }
    }
}
