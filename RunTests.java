public class RunTests {
    public static void main(String[] args) {
        GeneratedTests tests = new GeneratedTests();
        int passed = 0, failed = 0;

        System.out.println("\n===== RUNNING TESTS =====\n");

        // Run setup
        try {
            GeneratedTests.setup();
        } catch (Exception e) {
            System.out.println("Setup failed!");
            System.exit(1);
        }

        // Run Login test
        try {
            tests.test_Login();
            System.out.println("✓ Login Test PASSED");
            passed++;
        } catch (Exception e) {
            System.out.println("✗ Login Test FAILED: " + e.getMessage());
            failed++;
        }

        // Run GetUser test
        try {
            tests.test_GetUser();
            System.out.println("✓ GetUser Test PASSED");
            passed++;
        } catch (Exception e) {
            System.out.println("✗ GetUser Test FAILED: " + e.getMessage());
            failed++;
        }

        // Run UpdateUser test
        try {
            tests.test_UpdateUser();
            System.out.println("✓ UpdateUser Test PASSED");
            passed++;
        } catch (Exception e) {
            System.out.println("✗ UpdateUser Test FAILED: " + e.getMessage());
            failed++;
        }

        // Print results
        System.out.println("\n===== RESULTS =====");
        System.out.println("Total:  " + (passed + failed));
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("===================\n");
    }
}
