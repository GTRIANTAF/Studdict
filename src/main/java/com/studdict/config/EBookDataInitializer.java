package com.studdict.config;

import com.studdict.model.EBook;
import com.studdict.model.EBookLicense;
import com.studdict.repository.EBookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class EBookDataInitializer {

    @Bean
    public CommandLineRunner initEBooks(EBookRepository eBookRepository) {
        return args -> {
            if (eBookRepository.count() < 20) {
                String[][] books = {
                        // Computer Science & SWE
                        {"Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering"},
                        {"Design Patterns", "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides", "978-0201633610", "Software Engineering"},
                        {"Introduction to Algorithms", "Thomas H. Cormen, Charles E. Leiserson", "978-0262033848", "Computer Science"},
                        {"Artificial Intelligence", "Stuart Russell, Peter Norvig", "978-0134610993", "Computer Science"},
                        {"Operating System Concepts", "Abraham Silberschatz, Peter B. Galvin", "978-1119800361", "Computer Science"},
                        {"Computer Networking", "James Kurose, Keith Ross", "978-0133594140", "Computer Science"},
                        {"Database System Concepts", "Abraham Silberschatz", "978-0073523323", "Computer Science"},
                        
                        // Mathematics & Logic
                        {"Calculus", "James Stewart", "978-1285740621", "Mathematics"},
                        {"Linear Algebra and Its Applications", "David C. Lay", "978-0321982384", "Mathematics"},
                        {"Discrete Mathematics", "Kenneth H. Rosen", "978-0072899054", "Mathematics"},
                        
                        // Physics & Engineering
                        {"University Physics", "Hugh D. Young, Roger A. Freedman", "978-0321973610", "Physics"},
                        {"Fundamentals of Physics", "David Halliday, Robert Resnick", "978-1118230718", "Physics"},
                        {"Engineering Mechanics: Dynamics", "R. C. Hibbeler", "978-0133915389", "Engineering"},
                        {"Thermodynamics: An Engineering Approach", "Yunus A. Cengel", "978-1259822674", "Engineering"},
                        
                        // Business & Economics
                        {"Principles of Economics", "N. Gregory Mankiw", "978-1305585126", "Economics"},
                        {"Microeconomics", "Paul Krugman, Robin Wells", "978-1319098780", "Economics"},
                        {"Corporate Finance", "Stephen Ross, Randolph Westerfield", "978-0077861759", "Business"},
                        
                        // History & Humanities
                        {"Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "978-0062316097", "History"},
                        {"A People's History of the United States", "Howard Zinn", "978-0060838652", "History"}
                };

                for (String[] b : books) {
                    EBook ebook = new EBook();
                    ebook.setTitle(b[0]);
                    ebook.setAuthor(b[1]);
                    ebook.setIsbn(b[2]);
                    ebook.setCategory(b[3]);
                    ebook.setContent("This is the full digital text for \"" + b[0] + "\".\n\nLorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\n\n(End of text)");

                    // Give each book 2 licenses (2 copies available for loan)
                    EBookLicense license1 = new EBookLicense();
                    license1.setAvailable(true);
                    license1.setEbook(ebook);
                    
                    EBookLicense license2 = new EBookLicense();
                    license2.setAvailable(true);
                    license2.setEbook(ebook);

                    ebook.setLicenses(List.of(license1, license2));
                    
                    eBookRepository.save(ebook);
                }
            }
        };
    }
}
