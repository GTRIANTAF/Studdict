package com.studdict.mobile.api;

import com.studdict.mobile.model.Bill;
import com.studdict.mobile.model.EBook;
import com.studdict.mobile.model.EBookLoan;
import com.studdict.mobile.model.KitchenOrder;
import com.studdict.mobile.model.LoginRequest;
import com.studdict.mobile.model.MenuItem;
import com.studdict.mobile.model.Order;
import com.studdict.mobile.model.OrderItemRequest;
import com.studdict.mobile.model.OrderRequest;
import com.studdict.mobile.model.PublicReservation;
import com.studdict.mobile.model.RegisterRequest;
import com.studdict.mobile.model.ReservationRequest;
import com.studdict.mobile.model.Student;
import com.studdict.mobile.model.StudyTable;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StuddictApi {
    @GET("api/tables/available")
    Call<List<StudyTable>> getAvailableTables(
            @Query("venueId") long venueId,
            @Query("date") String date,
            @Query("time") String time,
            @Query("duration") int duration,
            @Query("minCapacity") int minCapacity
    );

    @GET("api/tables/matchmaking")
    Call<List<StudyTable>> getMatchmakingTables(
            @Query("venueId") long venueId,
            @Query("subjectName") String subjectName
    );

    @POST("api/tables/{tableId}/lock")
    Call<Boolean> lockTable(
            @Path("tableId") int tableId,
            @Query("studentId") String studentId
    );

    @POST("api/tables/{tableId}/unlock")
    Call<Void> unlockTable(@Path("tableId") int tableId);

    @POST("api/reservations/private")
    Call<Long> createPrivateReservation(@Body ReservationRequest request);

    @POST("api/reservations/public")
    Call<Long> createPublicReservation(@Body ReservationRequest request);

    @POST("api/reservations/{reservationId}/modify")
    Call<Boolean> modifyReservation(
            @Path("reservationId") long reservationId,
            @Query("time") String time,
            @Query("duration") int duration
    );

    @POST("payments/process")
    Call<com.studdict.mobile.model.PaymentResponse> processPayment(@Body com.studdict.mobile.model.PaymentRequest request);

    @POST("api/reservations/{reservationId}/join")
    Call<String> joinPublicReservation(
            @Path("reservationId") long reservationId,
            @Query("studentId") String studentId
    );

    // --- UC11 & UC12: Account Creation & Login ---
    @POST("api/students/register")
    Call<Student> registerStudent(@Body RegisterRequest request);

    @POST("api/students/login")
    Call<Student> loginStudent(@Body LoginRequest request);

    // --- UC7: Digital E-book Loan ---
    @POST("api/ebooks/access/{checkInId}")
    Call<Boolean> requestAccess(@Path("checkInId") long checkInId);

    @GET("api/ebooks/search")
    Call<List<EBook>> executeSearch(@Query("keyword") String keyword);

    // Gap 1: separate availability check before requestLoan
    @GET("api/ebooks/availability/{ebookId}")
    Call<Boolean> checkEBookAvailability(@Path("ebookId") long ebookId);

    @POST("api/ebooks/loan")
    Call<EBookLoan> requestLoan(@Query("checkInId") long checkInId, @Query("ebookId") long ebookId);

    @GET("api/ebooks/loan/{loanId}")
    Call<EBookLoan> getLoanStatus(@Path("loanId") long loanId);

    @POST("api/ebooks/return/{loanId}")
    Call<String> requestReturn(@Path("loanId") long loanId);

    // --- UC8: F&B Order ---
    @GET("api/orders/catalog")
    Call<List<MenuItem>> readCatalog();

    // Gap 5: addProduct per-item validation
    @POST("api/orders/cart/add")
    Call<Boolean> addCartItem(@Query("menuItemId") long menuItemId, @Query("quantity") int quantity);

    // Gap 6: processSummary before showing OrderReview
    @POST("api/orders/summary")
    Call<Boolean> processSummary(@Body List<OrderItemRequest> items);

    @POST("api/orders/create")
    Call<Order> createOrder(@Body OrderRequest request);

    // Gap 7: cancel order calls backend
    @POST("api/orders/cancel")
    Call<String> cancelOrder();

    // Gap 8: kitchen screen polling
    @GET("api/orders/kitchen/active")
    Call<List<KitchenOrder>> getKitchenOrders();

    // Gap 9: bill screen lookup
    @GET("api/bills/table/{tableId}")
    Call<Bill> getBillByTable(@Path("tableId") int tableId);

    @GET("api/liveboard/published")
    Call<List<PublicReservation>> getPublishedReservations();

    @GET("api/reservations/student/{studentId}")
    Call<List<Reservation>> getStudentReservations(@Path("studentId") String studentId);
}