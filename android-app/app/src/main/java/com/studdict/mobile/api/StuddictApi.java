package com.studdict.mobile.api;

import com.studdict.mobile.model.ReservationRequest;
import com.studdict.mobile.model.StudyTable;
import com.studdict.mobile.model.RegisterRequest;
import com.studdict.mobile.model.LoginRequest;
import com.studdict.mobile.model.Student;
import com.studdict.mobile.model.MenuItem;
import com.studdict.mobile.model.OrderRequest;
import com.studdict.mobile.model.Order;
import com.studdict.mobile.model.EBook;
import com.studdict.mobile.model.EBookLoan;

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

    @POST("api/ebooks/loan")
    Call<EBookLoan> requestLoan(@Query("checkInId") long checkInId, @Query("ebookId") long ebookId);

    @POST("api/ebooks/return/{loanId}")
    Call<String> requestReturn(@Path("loanId") long loanId);

    // --- UC8: F&B Order ---
    @GET("api/orders/catalog")
    Call<List<MenuItem>> readCatalog();

    @POST("api/orders/create")
    Call<Order> createOrder(@Body OrderRequest request);
}
