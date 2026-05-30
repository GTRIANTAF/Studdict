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
import com.studdict.mobile.model.OrderItemRequest;
import com.studdict.mobile.SessionManager;

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
    private TextView txtSubtotal;
    private Map<Long, Integer> cart = new HashMap<>();
    private List<MenuItem> menuItems = new ArrayList<>();

    // UC8 Gap 5: callback interface for addProduct validation
    interface OnAddValidationListener {
        void validate(long menuItemId, int newQuantity, Runnable onSuccess);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        if (session.getCheckInId() == -1L) {
            Toast.makeText(this, "You must be checked in to place an order.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_order_menu);

        recyclerMenu = findViewById(R.id.recyclerMenu);
        recyclerMenu.setLayoutManager(new LinearLayoutManager(this));
        txtSubtotal = findViewById(R.id.txtCartSubtotal);

        Button btnReviewOrder = findViewById(R.id.btnReviewOrder);
        btnReviewOrder.setOnClickListener(v -> {
            if (cart.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            // UC8 Gap 6: processSummary called before opening OrderReview (per sequence diagram)
            List<OrderItemRequest> items = buildOrderItems();
            ApiClient.getApi().processSummary(items).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful()) {
                        navigateToSummary();
                    } else {
                        Toast.makeText(ScreenOrderMenu.this, "Cannot proceed with order.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    navigateToSummary(); // Demo mode
                }
            });
        });

        fetchCatalog();
    }

    // UC8 Gap 5: validates item with backend before adding to local cart
    private void validateAndAddItem(long menuItemId, int newQty, Runnable onSuccess) {
        ApiClient.getApi().addCartItem(menuItemId, newQty).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                    onSuccess.run();
                } else {
                    Toast.makeText(ScreenOrderMenu.this, "Item not available.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                onSuccess.run(); // Demo mode: allow add
            }
        });
    }

    // UC8: Cart: updateSubtotal()
    private void updateSubtotal() {
        double total = 0;
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            for (MenuItem item : menuItems) {
                if (item.getItemId().equals(entry.getKey())) {
                    total += item.getPrice() * entry.getValue();
                    break;
                }
            }
        }
        txtSubtotal.setText(total == 0 ? "Cart: €0.00" : String.format("Cart: €%.2f", total));
    }

    private List<OrderItemRequest> buildOrderItems() {
        List<OrderItemRequest> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            items.add(new OrderItemRequest(entry.getKey(), entry.getValue()));
        }
        return items;
    }

    private void navigateToSummary() {
        Map<Long, MenuItem> itemMap = new HashMap<>();
        for (MenuItem item : menuItems) itemMap.put(item.getItemId(), item);

        ArrayList<String> cartItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            MenuItem item = itemMap.get(entry.getKey());
            String name = item != null ? item.getName() : "Item #" + entry.getKey();
            double price = item != null ? item.getPrice() : 0.0;
            cartItems.add(entry.getKey() + "|" + entry.getValue() + "|" + name + "|" + price);
        }
        Intent intent = new Intent(this, ScreenOrderSummary.class);
        intent.putStringArrayListExtra("CART_ITEMS", cartItems);
        intent.putExtra("TABLE_ID", getIntent().getLongExtra("TABLE_ID", 1L));
        intent.putExtra("STUDENT_ID", getIntent().getStringExtra("STUDENT_ID"));
        startActivity(intent);
    }

    private void fetchCatalog() {
        ApiClient.getApi().readCatalog().enqueue(new Callback<List<MenuItem>>() {
            @Override
            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadMenu(response.body());
                } else {
                    loadDummyMenu();
                }
            }

            @Override
            public void onFailure(Call<List<MenuItem>> call, Throwable t) {
                loadDummyMenu();
            }
        });
    }

    private void loadMenu(List<MenuItem> items) {
        menuItems = items;
        adapter = new MenuAdapter(menuItems, cart, this::updateSubtotal, this::validateAndAddItem);
        recyclerMenu.setAdapter(adapter);
    }

    private void loadDummyMenu() {
        loadMenu(getDummyMenuItems());
        Toast.makeText(this, "Offline: showing demo menu.", Toast.LENGTH_SHORT).show();
    }

    private List<MenuItem> getDummyMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String[][] data = {
            {"1", "Espresso",       "Strong black coffee",        "2.50"},
            {"2", "Cappuccino",     "Coffee with steamed milk",   "3.20"},
            {"3", "Club Sandwich",  "Chicken, lettuce, tomato",   "5.50"},
            {"4", "Croissant",      "Buttery French pastry",      "2.00"},
            {"5", "Orange Juice",   "Freshly squeezed",           "3.00"},
            {"6", "Mineral Water",  "Still or sparkling",         "1.50"},
        };
        for (String[] d : data) {
            MenuItem item = new MenuItem();
            item.setItemId(Long.parseLong(d[0]));
            item.setName(d[1]);
            item.setDescription(d[2]);
            item.setPrice(Double.parseDouble(d[3]));
            items.add(item);
        }
        return items;
    }

    // --- Adapter ---
    private static class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
        private final List<MenuItem> items;
        private final Map<Long, Integer> cart;
        private final Runnable onCartChanged;
        private final OnAddValidationListener onAdd;

        MenuAdapter(List<MenuItem> items, Map<Long, Integer> cart,
                    Runnable onCartChanged, OnAddValidationListener onAdd) {
            this.items = items;
            this.cart = cart;
            this.onCartChanged = onCartChanged;
            this.onAdd = onAdd;
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
            holder.txtPrice.setText(String.format("€%.2f", item.getPrice()));

            Integer qtyVal = cart.get(item.getItemId());
            holder.txtQty.setText(String.valueOf(qtyVal != null ? qtyVal : 0));

            holder.btnAdd.setOnClickListener(v -> {
                Integer qVal = cart.get(item.getItemId());
                int newQty = (qVal != null ? qVal : 0) + 1;
                // UC8 Gap 5: validate with backend before committing to local cart
                onAdd.validate(item.getItemId(), newQty, () -> {
                    cart.put(item.getItemId(), newQty);
                    notifyItemChanged(position);
                    onCartChanged.run();
                });
            });

            holder.btnRemove.setOnClickListener(v -> {
                Integer qVal = cart.get(item.getItemId());
                int q = qVal != null ? qVal : 0;
                if (q > 0) {
                    q--;
                    if (q == 0) cart.remove(item.getItemId());
                    else cart.put(item.getItemId(), q);
                    notifyItemChanged(position);
                    onCartChanged.run();
                }
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

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
