package com.financial.solutions.incomesynchronization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = IncomeSynchronizationApplication.class)
class IncomeSynchronizationApplicationTests {

	@TempDir
	static Path tempDir;

//	@BeforeAll
//	private void generateTestFiles() {
//		Path successFile = tempDir.resolve("");
//	}

	@Test
	void testmainCallTestWithNoArguments() {
		IncomeSynchronizationApplication.main(new String[] {});
	}

	@Test
	void testMainCallWithManyArguments() {
		IncomeSynchronizationApplication.main(new String[] {"", "", ""});
	}

	@Test
	void testMainCallWithValidFile() throws IOException {
		Path file = tempDir.resolve("testFileOk.csv");

		List<String> lines = new ArrayList<>();
		lines.add("agencia;conta;saldo;status");
		lines.add("0101;12225-6;100,00;A");

		Files.write(file, lines);

		IncomeSynchronizationApplication.main(new String[] {file.toString()});

		Files.deleteIfExists(Paths.get("Proc_"+file.getFileName()));
	}

	@Test
	void testMainCallWithMissigArgumentFile() throws IOException {
		Path file = tempDir.resolve("missingArgument.csv");

		List<String> lines = new ArrayList<>();
		lines.add("agencia;conta;saldo;status");
		lines.add("0101;100,00;A");

		Files.write(file, lines);

		IncomeSynchronizationApplication.main(new String[] {file.toString()});

		Files.deleteIfExists(Paths.get("Proc_"+file.getFileName()));
	}

	@Test
	void testMainCallWithInvalidDoubleFile() throws IOException {
		Path file = tempDir.resolve("testFileOk.csv");

		List<String> lines = new ArrayList<>();
		lines.add("agencia;conta;saldo;status");
		lines.add("0101;12225-6;e100,00;A");

		Files.write(file, lines);

		IncomeSynchronizationApplication.main(new String[] {file.toString()});

		Files.deleteIfExists(Paths.get("Proc_"+file.getFileName()));
	}

	@Test
	void testMainCallWithInvalidHeaderFile() throws IOException {
		Path file = tempDir.resolve("testFileOk.csv");

		List<String> lines = new ArrayList<>();
		lines.add("4g3nc14;cont4;sald0;st4tus");
		lines.add("0101;12225-6;100,00;A");

		Files.write(file, lines);

		IncomeSynchronizationApplication.main(new String[] {file.toString()});

		Files.deleteIfExists(Paths.get("Proc_"+file.getFileName()));
	}

	@Test
	void testMainCallWithDifferentExtensionFile() throws IOException {
		Path file = tempDir.resolve("testFile.txt");

		List<String> lines = new ArrayList<>();
		lines.add("agencia;conta;saldo;status");
		lines.add("0101;12225-6;100,00;A");

		Files.write(file, lines);

		IncomeSynchronizationApplication.main(new String[] {file.toString()});

		Files.deleteIfExists(Paths.get("Proc_"+file.getFileName()));
	}
}
