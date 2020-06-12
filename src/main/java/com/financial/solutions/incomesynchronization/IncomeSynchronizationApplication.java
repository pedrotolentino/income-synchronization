package com.financial.solutions.incomesynchronization;

import com.financial.solutions.incomesynchronization.dto.IncomeFileInputDto;
import com.financial.solutions.incomesynchronization.exception.BusinessException;
import com.financial.solutions.incomesynchronization.service.ReceitaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SpringBootApplication
public class IncomeSynchronizationApplication implements CommandLineRunner {
	private static final String DATA_SEPARATOR = ";";
	private static final String NEXT_LINE_CHARACTER = "\n";
	private static final String OUTPUT_HEADER = "agencia;conta;saldo;status;resultado";
	private static final Logger LOGGER = LoggerFactory.getLogger(IncomeSynchronizationApplication.class);

	@Autowired
	private MessageSource messageSource;

	private static Queue<String> rejectedLines;

	public static void main(String[] args) {
		SpringApplication.run(IncomeSynchronizationApplication.class, args);
	}

	public void run(String... args){

		LOGGER.info("");
		LOGGER.info(getPropertyMessage("track.info.init"));

		try{
			switch (args.length) {
				case 0:
					throw new BusinessException(getPropertyMessage("exception.empty.argument"));
				case 1:
					processIncomeSynchronization(args[0]);
					break;
				default:
					throw new BusinessException(getPropertyMessage("exception.multiple.arguments"));
			}
		} catch (BusinessException | InterruptedException exception) {
			LOGGER.error(exception.getMessage());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			loggingProcessInformation();
		}
	}

	private void loggingProcessInformation() {
		if (!getRejectedLines().isEmpty()) {
			LOGGER.warn(getPropertyMessage("track.warn.lines.not.processed"));
			LOGGER.warn("");
			getRejectedLines().forEach(LOGGER::error);
			LOGGER.warn("");
			LOGGER.warn(getPropertyMessage("track.warn.review.suggestion"));
		}

		LOGGER.info("");
		LOGGER.info(getPropertyMessage("track.info.final.message"));
	}

	private void processIncomeSynchronization(String filePath) throws InterruptedException, BusinessException {
		LOGGER.info(getPropertyMessage("track.info.income.processing"));
		List<IncomeFileInputDto> incomesToSync= loadFile(filePath);

		ReceitaService service = new ReceitaService();

		LOGGER.info(getPropertyMessage("track.info.creating.output.file"));
		String outputFileName = "Proc_"+Paths.get(filePath).getFileName();

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFileName))) {
			writer.write(OUTPUT_HEADER);
			writer.write(NEXT_LINE_CHARACTER);

			LOGGER.info(getPropertyMessage("track.info.processing.income.service"));
			for (IncomeFileInputDto income : incomesToSync) {
				boolean resultProc;
				try {
					resultProc = service.atualizarConta(income.getBankBranch(),
							                            income.getBankAccount().replace("-", ""),
							                            income.getBankBalance(),
							                            income.getStatus());
				} catch (RuntimeException r) {
					resultProc = false;
				}

				StringBuilder line = new StringBuilder();
				line.append(income.getBankBranch()).append(DATA_SEPARATOR);
				line.append(income.getBankAccount()).append(DATA_SEPARATOR);
				line.append(String.valueOf(income.getBankBalance()).replace(".", ",")).append(DATA_SEPARATOR);
				line.append(income.getStatus()).append(DATA_SEPARATOR);
				line.append(resultProc ? getPropertyMessage("success.processing") : getPropertyMessage("fail.processing")).append(NEXT_LINE_CHARACTER);

				writer.write(line.toString());
			}
			LOGGER.info(getPropertyMessage("track.info.close.output.file"));
		} catch (IOException i) {
			throw new BusinessException(getPropertyMessage("exception.writing.file", i.getMessage()));
		}
	}

	private List<IncomeFileInputDto> loadFile(String archive) throws BusinessException {
		if (archive.isEmpty()) {
			throw new BusinessException(getPropertyMessage("exception.empty.path"));
		}

		if (!archive.endsWith(".csv")) {
			throw new BusinessException(getPropertyMessage("exception.invalid.extension"));
		}

		List<IncomeFileInputDto> result = new LinkedList<>();

		Path path = Paths.get(archive);

		List<String> lines;

		try {
			lines = Files.readAllLines(path);
		} catch (IOException i) {
			throw new BusinessException(getPropertyMessage("exception.reading.file", i.getMessage()));
		}

		if (validateHeader(lines.get(0))) {
			for (int i = 1; i < lines.size(); i++) {
				String[] info = lines.get(i).split(";");

				if (info.length != 4) {
					getRejectedLines().add(getPropertyMessage("reject.file.line.missing.info",
							String.valueOf(i+1), String.valueOf(info.length)));
					continue;
				}

				IncomeFileInputDto dto = new IncomeFileInputDto();

				dto.setBankBranch(info[0]);
				dto.setBankAccount(info[1]);
				try {
					dto.setBankBalance(Double.parseDouble(info[2].replace(",",".")));
				} catch (NumberFormatException n) {
					getRejectedLines().add(getPropertyMessage("reject.file.line.wrong.balance",
							String.valueOf(i+1)));
					continue;
				}
				dto.setStatus(info[3]);

				result.add(dto);
			}
		}

		return result;
	}

	private boolean validateHeader(String header) {
		return OUTPUT_HEADER.replace(";resultado", "").equals(header);
	}

	private String getPropertyMessage(String property) {
		return messageSource.getMessage(property, null, Locale.getDefault());
	}

	private String getPropertyMessage(String property, String... params) {
		return messageSource.getMessage(property, params, Locale.getDefault());
	}

	private Queue<String> getRejectedLines() {
		if (rejectedLines == null) {
			rejectedLines = new LinkedList<String>();
		}

		return rejectedLines;
	}
}
