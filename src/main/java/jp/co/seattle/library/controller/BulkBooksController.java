package jp.co.seattle.library.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jp.co.seattle.library.dto.BookDetailsInfo;
import jp.co.seattle.library.service.BooksService;

/**
 * 一括登録画面に遷移する
 */
@Controller
public class BulkBooksController {

	@Autowired
	private BooksService booksService;

	@RequestMapping(value = "/bulkBook", method = RequestMethod.GET) // value＝actionで指定したパラメータ
	// RequestParamでname属性を取得
	public String login(Model model) {
		return "bulkBook";
	}

	/**
	 * 一括登録コントローラー
	 * 
	 * @param file
	 * @return bulkbook
	 */
	@Transactional
	@RequestMapping(value = "/bulkRegist", method = RequestMethod.POST)
	public String uploadFile(@RequestParam("file") MultipartFile File, Model model) {

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(File.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			int count = 0;

			List<String[]> booksList = new ArrayList<String[]>();
			List<Integer> errorlist = new ArrayList<Integer>();

			if (!br.ready()) {
				model.addAttribute("addErrorMessage", "CSVに書籍情報がありません。");
				return "bulkBook";
			}

			while ((line = br.readLine()) != null) {
				count = count + 1;
				final String[] split = line.split(",", -1);

				// 書籍情報バリデーションチェック
				if (StringUtils.isEmpty(split[0]) || StringUtils.isEmpty(split[1]) || StringUtils.isEmpty(split[2])
						|| StringUtils.isEmpty(split[3]) || !(split[3].matches("^[0-9]{8}"))
						|| split[4].length() != 0 && !(split[4].matches("^[0-9]{10}|[0-9]{13}"))) {
					errorlist.add(count);

				} else {
					booksList.add(split);
				}
			}

			if (errorlist.size() > 0) {
				List<String> addErrorMessage = new ArrayList<String>();
				for (int i = 0; i < errorlist.size(); i++) {
					addErrorMessage.add(errorlist.get(i) + "行目にエラーがあります。");
				}

				model.addAttribute("addErrorMessage", addErrorMessage);
				return "bulkBook";
			}

			for (int i = 0; i < booksList.size(); i++) {
				String[] bookList = booksList.get(i);

				BookDetailsInfo bookInfo = new BookDetailsInfo();
				bookInfo.setTitle(bookList[0]);
				bookInfo.setAuthor(bookList[1]);
				bookInfo.setPublisher(bookList[2]);
				bookInfo.setPublishDate(bookList[3]);
				bookInfo.setISBN(bookList[4]);

				// 書籍情報に一括登録する
				booksService.registBook(bookInfo);

			}

		} catch (IOException e) {
			throw new RuntimeException("ファイルが読み込めません", e);
		}

		return "redirect:home";
	}
}
