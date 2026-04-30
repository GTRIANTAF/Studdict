import java.util.List;
import java.util.ArrayList;

public class EBook {
    private String eBookId;
    private String title;
    private String author;
    private String isbn;
    private String category;

    private List<EBookLicense> licenses = new ArrayList<>();
}