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
                    ebook.setContent(contentFor(b[0]));

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

    // Pages are stored in a single TEXT column, separated by the form-feed character ('\f').
    // The E-book Reader splits on it to paginate. Some books get rich multi-page content;
    // the rest get a short two-page default so every loaned book is still readable.
    private static final String PAGE_BREAK = "\f";

    private static String pages(String... pages) {
        return String.join(PAGE_BREAK, pages);
    }

    private static String contentFor(String title) {
        switch (title) {
            case "Clean Code":
                return pages(
                    "Clean Code — Robert C. Martin\n\nChapter 1: Clean Code\n\n" +
                        "You are reading this book for two reasons. First, you are a programmer. " +
                        "Second, you want to be a better programmer. Good. We need better programmers.",
                    "Page 2\n\nThere are two parts to learning craftsmanship: knowledge and work. " +
                        "You must gain the knowledge of principles, patterns, practices, and heuristics " +
                        "that a craftsman knows, and you must also grind that knowledge into your fingers, " +
                        "eyes, and gut by working hard and practicing.",
                    "Page 3\n\nThe only way to go fast is to keep the code clean. Bad code tempts the mess " +
                        "to grow! When others change bad code, they tend to make it worse. A clean code base " +
                        "lets a team keep moving quickly, sprint after sprint.",
                    "Page 4\n\nClean code is simple and direct. Clean code reads like well-written prose. " +
                        "Clean code never obscures the designer's intent but rather is full of crisp " +
                        "abstractions and straightforward lines of control.",
                    "Page 5\n\nMeaningful names, small functions that do one thing, and tests that keep the " +
                        "code flexible are the foundation. The rest of this book is full of concrete examples.\n\n" +
                        "(End of sample)");
            case "Introduction to Algorithms":
                return pages(
                    "Introduction to Algorithms\n\nChapter 1: The Role of Algorithms in Computing\n\n" +
                        "An algorithm is any well-defined computational procedure that takes some value, or set " +
                        "of values, as input and produces some value, or set of values, as output.",
                    "Page 2\n\nThe sorting problem: given a sequence of n numbers, output a permutation " +
                        "(reordering) of the input sequence such that the numbers are in nondecreasing order. " +
                        "Sorting is a fundamental operation in computer science.",
                    "Page 3\n\nWe measure efficiency using asymptotic notation. We say insertion sort runs in " +
                        "O(n^2) time in the worst case, while merge sort runs in O(n log n) time. For large " +
                        "inputs, the difference is dramatic.",
                    "Page 4\n\nDivide-and-conquer algorithms break a problem into subproblems, solve the " +
                        "subproblems recursively, and then combine the solutions. Merge sort is a classic " +
                        "example of this powerful technique.\n\n(End of sample)");
            case "Sapiens: A Brief History of Humankind":
                return pages(
                    "Sapiens — Yuval Noah Harari\n\nPart One: The Cognitive Revolution\n\n" +
                        "About 13.5 billion years ago, matter, energy, time and space came into being in what " +
                        "is known as the Big Bang. The story of these fundamental features is called physics.",
                    "Page 2\n\nAround 70,000 years ago, organisms belonging to the species Homo sapiens started " +
                        "to form even more elaborate structures called cultures. The subsequent development of " +
                        "these human cultures is called history.",
                    "Page 3\n\nThe ability to speak about fictions is the most unique feature of Sapiens " +
                        "language. Legends, myths and shared beliefs allowed large numbers of strangers to " +
                        "cooperate flexibly. That is why Sapiens rules the world.",
                    "Page 4\n\nThe Agricultural Revolution, beginning around 12,000 years ago, let humans grow " +
                        "food in greater quantities — but it also bound them to a harder, riskier way of life.\n\n" +
                        "(End of sample)");
            case "Principles of Economics":
                return pages(
                    "Principles of Economics — N. Gregory Mankiw\n\nChapter 1: Ten Principles of Economics\n\n" +
                        "The word economy comes from the Greek for 'one who manages a household.' Like a " +
                        "household, a society faces many decisions about how to allocate scarce resources.",
                    "Page 2\n\nPrinciple 1: People face trade-offs. To get one thing we usually have to give up " +
                        "another. Making decisions requires trading off one goal against another.",
                    "Page 3\n\nPrinciple 2: The cost of something is what you give up to get it. Because people " +
                        "face trade-offs, making decisions requires comparing the costs and benefits of " +
                        "alternative courses of action.\n\n(End of sample)");
            case "Calculus":
                return pages(
                    "Calculus — James Stewart\n\nChapter 1: Functions and Models\n\n" +
                        "The fundamental objects that we deal with in calculus are functions. A function " +
                        "expresses how one quantity depends on another.",
                    "Page 2\n\nThe derivative measures the instantaneous rate of change of a function. " +
                        "Geometrically, it is the slope of the tangent line to the curve at a point.",
                    "Page 3\n\nThe integral, the second great idea of calculus, measures accumulation — the area " +
                        "under a curve. The Fundamental Theorem of Calculus links derivatives and integrals.\n\n" +
                        "(End of sample)");
            default:
                return pages(
                    "\"" + title + "\"\n\nThis is the opening page of the digital sample for this title. " +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor " +
                        "incididunt ut labore et dolore magna aliqua.",
                    "Page 2\n\nUt enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut " +
                        "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit.\n\n(End of sample)");
        }
    }
}
