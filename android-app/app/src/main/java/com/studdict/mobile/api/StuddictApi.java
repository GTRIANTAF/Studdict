package com.studdict.mobile.api;

import com.studdict.mobile.model.Bill;
import com.studdict.mobile.model.CheckIn;
import com.studdict.mobile.model.CheckInRequest;
import com.studdict.mobile.model.CheckInResponse;
import com.studdict.mobile.model.ConfirmCheckInRequest;
import com.studdict.mobile.model.EBook;
import com.studdict.mobile.model.EBookLoan;
import com.studdict.mobile.model.EBookLoanInfo;
import com.studdict.mobile.model.GenerateInviteCodeRequest;
import com.studdict.mobile.model.InviteCode;
import com.studdict.mobile.model.InviteJoinResponse;
import com.studdict.mobile.model.JoinInviteCodeRequest;
import com.studdict.mobile.model.KitchenOrder;
import com.studdict.mobile.model.LoginRequest;
import com.studdict.mobile.model.MenuItem;
import com.studdict.mobile.model.Order;
import com.studdict.mobile.model.OrderItemRequest;
import com.studdict.mobile.model.OrderRequest;
import com.studdict.mobile.model.PublicReservation;
import com.studdict.mobile.model.RegisterRequest;
import com.studdict.mobile.model.Reservation;
import com.studdict.mobile.model.ReservationParticipant;
import com.studdict.mobile.model.ReservationRequest;
import com.studdict.mobile.model.Student;
import com.studdict.mobile.model.StudyTable;
import com.studdict.mobile.model.ValidateCheckInRequest;
import com.studdict.mobile.model.ValidateInviteCodeRequest;
import com.studdict.mobile.model.LoyaltyWallet;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    @GET("api/gamification/wallet/{studentId}")
    Call<LoyaltyWallet> getWallet(@Path("studentId") String studentId);

    @POST("api/gamification/validate")
    Call<Boolean> validatePoints(
            @Query("studentId") String studentId,
            @Query("points") int points
    );

    @POST("api/gamification/redeem")
    Call<okhttp3.ResponseBody> redeemPoints(
            @Query("studentId") String studentId,
            @Query("pointsToRedeem") int points,
            @Query("tableId") Integer tableId
    );

    @POST("api/gamification/earn")
    Call<okhttp3.ResponseBody> earnPoints(
            @Query("studentId") String studentId,
            @Query("reservationId") long reservationId
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

    @POST("api/reservations/{reservationId}/join")
    Call<String> joinPublicReservation(
            @Path("reservationId") long reservationId,
            @Query("studentId") String studentId
    );

    @PUT("api/reservations/{id}/modify")
    Call<com.studdict.mobile.model.Reservation> modifyReservation(
            @Path("id") long id,
            @Body com.studdict.mobile.model.ReservationUpdateRequest request
    );

    // --- UC3: Invite Code Reservation Join ---
    @POST("invite-code/generate")
    Call<InviteCode> generateInviteCode(@Body GenerateInviteCodeRequest request);

    @POST("invite-code/validate")
    Call<Boolean> validateInviteCode(@Body ValidateInviteCodeRequest request);

    @POST("invite-code/join")
    Call<Boolean> joinReservationWithInviteCode(@Body JoinInviteCodeRequest request);

    @POST("invite-code/join-result")
    Call<InviteJoinResponse> joinReservationWithInviteCodeResult(@Body JoinInviteCodeRequest request);

    // --- UC5: Check-in with reservation and QR ---
    @POST("check-in/validate")
    Call<String> validateCheckIn(@Body ValidateCheckInRequest request);

    @GET("check-in/participants")
    Call<List<ReservationParticipant>> getCheckInParticipants(@Query("reservationId") long reservationId);

    @POST("check-in/confirm")
    Call<CheckInResponse> confirmCheckIn(@Body ConfirmCheckInRequest request);

    @POST("check-in/perform")
    Call<CheckInResponse> performCheckIn(@Body CheckInRequest request);

    // --- UC11 & UC12: Account Creation & Login ---
    @POST("api/students/register")
    Call<Student> registerStudent(@Body RegisterRequest request);

    @POST("api/students/login")
    Call<Student> loginStudent(@Body LoginRequest request);

    // --- UC7: Digital E-book Loan ---
    @POST("api/ebooks/access/{checkInId}")
    Call<Boolean> requestAccess(@Path("checkInId") long checkInId);

    @GET("api/ebooks/catalog")
    Call<List<EBook>> getCatalog(@Query("keyword") String keyword);

    @GET("api/ebooks/search")
    Call<List<EBook>> executeSearch(@Query("keyword") String keyword);

    @GET("api/ebooks/availability/{ebookId}")
    Call<Boolean> checkEBookAvailability(@Path("ebookId") long ebookId);

    // UC7 step 7: full book content (paginated) for the e-book reader
    @GET("api/ebooks/{ebookId}/content")
    Call<com.studdict.mobile.model.EBookContent> getBookContent(@Path("ebookId") long ebookId);

    @POST("api/ebooks/loan")
    Call<EBookLoan> requestLoan(@Query("checkInId") long checkInId, @Query("ebookId") long ebookId);

    @GET("api/ebooks/loan/{loanId}")
    Call<EBookLoan> getLoanStatus(@Path("loanId") long loanId);

    @POST("api/ebooks/return/{loanId}")
    Call<String> requestReturn(@Path("loanId") long loanId);

    @GET("api/ebooks/loans/active/{checkInId}")
    Call<List<EBookLoanInfo>> getActiveLoans(@Path("checkInId") long checkInId);

    // All loans of the session (active + returned) — shown on the bill screen at checkout
    @GET("api/ebooks/loans/session/{checkInId}")
    Call<List<EBookLoanInfo>> getSessionLoans(@Path("checkInId") long checkInId);

    @POST("api/ebooks/notify-checkout/{checkInId}")
    Call<String> notifyCheckout(@Path("checkInId") long checkInId);

    // --- UC8: F&B Order ---
    @GET("api/orders/catalog")
    Call<List<MenuItem>> readCatalog();

    @POST("api/orders/cart/add")
    Call<Boolean> addCartItem(@Query("menuItemId") long menuItemId, @Query("quantity") int quantity);

    @POST("api/orders/summary")
    Call<Boolean> processSummary(@Body List<OrderItemRequest> items);

    @POST("api/orders/create")
    Call<Order> createOrder(@Body OrderRequest request);

    @POST("api/orders/cancel")
    Call<String> cancelOrder();

    @GET("api/orders/kitchen/active")
    Call<List<KitchenOrder>> getKitchenOrders();

    @GET("api/bills/table/{tableId}")
    Call<Bill> getBillByTable(@Path("tableId") int tableId);

    @GET("api/liveboard/published")
    Call<List<PublicReservation>> getPublishedReservations();

    @GET("api/reservations/student/{studentId}")
    Call<List<Reservation>> getStudentReservations(@Path("studentId") String studentId);
}
