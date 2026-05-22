package com.studdict.mobile.api;

import com.studdict.mobile.model.ReservationRequest;
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

    @POST("api/reservations/{reservationId}/join")
    Call<String> joinPublicReservation(
            @Path("reservationId") long reservationId,
            @Query("studentId") String studentId
    );
}
