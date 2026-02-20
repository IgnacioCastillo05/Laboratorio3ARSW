package edu.eci.arsw.blueprints.dto;

/**
 * Respuesta uniforme para todos los endpoints de la API.
 *
 * @param <T> tipo del dato retornado
 */
public record ApiResponse<T>(int code, String message, T data) {

    /** 200 OK — consultas exitosas */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "execute ok", data);
    }

    /** 201 Created — creación exitosa */
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "resource created", data);
    }

    /** 202 Accepted — actualización exitosa */
    public static <T> ApiResponse<T> accepted(T data) {
        return new ApiResponse<>(202, "update accepted", data);
    }

    /** 400 Bad Request — datos inválidos */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null);
    }

    /** 404 Not Found — recurso inexistente */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message, null);
    }

    /** 409 Conflict / 403 Forbidden — recurso ya existe */
    public static <T> ApiResponse<T> conflict(String message) {
        return new ApiResponse<>(409, message, null);
    }
}