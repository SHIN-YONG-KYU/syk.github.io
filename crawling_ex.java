
public class Croling {
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		//로그인정보
		String userName ="##########";
		String psssword ="##########";

		//로그인을 위한 폼데이터
		Map<String, String> data = new HashMap<String,String>();
		data.put("id", userName);
		data.put("pw", psssword);
		data.put("auto", "N");

		//로그인 post
		Connection.Response response = Jsoup.connect("http://t-g-o.net/back/adm/login.php")
										.data(data)
										.method(Connection.Method.POST)
										.execute();

		//로그인 성공후 얻은 쿠키
		Map<String, String> loginCookie = response.cookies();
		System.out.println("쿠키정보 ::"+loginCookie.toString());
		
		//전체리스트
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
		
		//반복문 시작
		for(int i=1 ; i<=13243 ; i++) {
			
			System.out.println("=========================================");
			System.out.println();
			
			//크롤링할 url 주소 (각개별 회원정보) 전체회원 1부터 반복
			String url = "##########;
			System.out.println("크롤링한 url 주소 ::"+url);
			
			//고객정보
			Map<String, Object> adminInfo = new HashMap<String,Object>();
			
			try {
				Document doc = Jsoup.connect(url)
						.cookies(loginCookie)
						.get();
				
				//개별 고객 해쉬맵
				String userNo = doc.select(".userinfo_wrap > table > tbody > tr > td > span").get(0).text();
				String regDate = doc.select(".userinfo_wrap > table > tbody > tr > td > span").get(1).text();
				String adminId = doc.select("#my_id0").get(0).text();
				String adminHp = doc.select("#my_phone0").get(0).text();
				String adminEmail = doc.select("#my_email0").get(0).text();
				String recommendName = doc.select(".userinfo_tb > tbody > tr > td > div > span").get(3).text();
				//추천인이 없을경우
				if(recommendName.equals("")) {
					recommendName = "x";
				}
				String nowMoney = doc.select(".nowMoney").get(0).text();
				String nowPoint = doc.select(".nowPoint").get(0).text();
				String holderName = doc.select("#my_holder0").get(0).text();
				String bank = doc.select("#my_bank0").get(0).text();
				String account = doc.select("#my_account0").get(0).text();
				String use_status = doc.select(".user_state").get(0).text();
				
				//유저 정보 존해하는지 확인
				if(adminId.isEmpty()) {
					System.out.println(i+" 번 유저정보 비어있음.");
				}else {
					
					adminInfo.put("userNo", userNo);
					adminInfo.put("regDate", regDate);
					adminInfo.put("adminId", adminId);
					adminInfo.put("adminHp", adminHp);
					adminInfo.put("adminEmail", adminEmail);
					adminInfo.put("recommendName", recommendName);
					adminInfo.put("nowMoney", nowMoney);
					adminInfo.put("nowPoint", nowPoint);
					adminInfo.put("holderName", holderName);
					adminInfo.put("bank", bank);
					adminInfo.put("account", account);
					adminInfo.put("use_status", use_status);
					
					System.out.println("- 유저번호 ::"+userNo);
					System.out.println("- 가입일 ::"+regDate);
					System.out.println("- 회원 ID ::"+doc.select("#my_id0").get(0).text());
					System.out.println("- 전화번호 ::"+doc.select("#my_phone0").get(0).text());
					System.out.println("- 이메일 ::"+doc.select("#my_email0").get(0).text());
					System.out.println("- 추천인 ::"+doc.select(".userinfo_tb > tbody > tr > td > div > span").get(3).text());
					System.out.println("- 보유번개 ::"+doc.select(".nowMoney").get(0).text());
					System.out.println("- 보유포인트 ::"+doc.select(".nowPoint").get(0).text());
					System.out.println("- 예금주 ::"+doc.select("#my_holder0").get(0).text());
					System.out.println("- 은행명 ::"+doc.select("#my_bank0").get(0).text());
					System.out.println("- 계좌번호 ::"+doc.select("#my_account0").get(0).text());
					System.out.println("- 회원상태 ::"+doc.select(".user_state").get(0).text());
					
					
					try {
						//캐릭터 보유현황
						System.out.println("display_sum index ::"+doc.toString().indexOf("display_sum"));
						
						//스크립트에 있는 합계데이터를 찾아서 ""로 분리
						int index_display_sum = doc.toString().indexOf("display_sum");
						String source =doc.toString().substring(index_display_sum,index_display_sum+440); //index 번호를 구하고 거기에 +445더한 사이 문자열을 구함
						Pattern pattern = Pattern.compile("[\"](.*?)[\"]");
						Matcher matcher = pattern.matcher(source);
						
						int count =0;
						while (matcher.find()) {  // 일치하는 게 있다면
							//System.out.println("------------일치하는 그룹 -------------");
							
							if(count == 1) {
								System.out.println("- 하데스 합계 :"+matcher.group(1));
								adminInfo.put("hades",matcher.group(1));
							}else if(count == 2) {
								System.out.println("- 포세이돈 합계 :"+matcher.group(1));
								adminInfo.put("poseidon",matcher.group(1));
							}else if(count == 3) {
								System.out.println("- 제우스 합계 :"+matcher.group(1));
								adminInfo.put("zeus",matcher.group(1));
							}else if(count == 5){
								System.out.println("- 총합계 합계 :"+matcher.group(1));
								adminInfo.put("total",matcher.group(1));
							}else {
								//System.out.println(matcher.group(1));
							}
							
							count++;
							if(matcher.group(1) ==  null)
								break;
						}
					} catch (IndexOutOfBoundsException e1) {
						// TODO: handle exception
						adminInfo.put("hades","예외");
						adminInfo.put("poseidon","예외");
						adminInfo.put("zeus","예외");
						adminInfo.put("total","예외");
					}
					list.add((HashMap<String, Object>) adminInfo);
					
				}//else
				
			} catch (Exception e) {
				
				//예외 발생시 모든컬럼 예외처리
				adminInfo.put("userNo", "예외");
				adminInfo.put("regDate", "예외");
				adminInfo.put("adminId", "예외");
				adminInfo.put("adminHp", "예외");
				adminInfo.put("adminEmail", "예외");
				adminInfo.put("recommendName", "예외");
				adminInfo.put("nowMoney", "예외");
				adminInfo.put("nowPoint", "예외");
				adminInfo.put("holderName", "예외");
				adminInfo.put("bank", "예외");
				adminInfo.put("account", "예외");
				adminInfo.put("use_status", "예외");
				
				//point
				adminInfo.put("hades","예외");
				adminInfo.put("poseidon","예외");
				adminInfo.put("zeus","예외");
				adminInfo.put("total","예외");
				
				System.out.println("----------------컬럼 예외처리 -------");
				list.add((HashMap<String, Object>) adminInfo);
				
			}
			
			
			
			System.out.println();
			System.out.println("=========================================");
				
		}//for
				
				
		//----------------------------------------------
		//파일 엑셀다운로드
		System.out.println("=========================================");
		System.out.println();
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("##########");
		//sheet.autoSizeColumn(0);
		sheet.setColumnWidth(2,6000);
		sheet.setColumnWidth(3,6000);
		sheet.setColumnWidth(4,6000);
		sheet.setColumnWidth(5,6000);
		sheet.setColumnWidth(6,6000);
		sheet.setColumnWidth(7,6000);
		sheet.setColumnWidth(8,6000);
		sheet.setColumnWidth(9,6000);
		sheet.setColumnWidth(10,6000);
		sheet.setColumnWidth(11,6000);
		sheet.setColumnWidth(12,6000);
		sheet.setColumnWidth(13,6000);
		sheet.setColumnWidth(14,6000);
		sheet.setColumnWidth(15,6000);


		CellStyle s = wb.createCellStyle();
		s.setAlignment(HorizontalAlignment.CENTER);
		s.setVerticalAlignment(VerticalAlignment.CENTER);
		s.setBorderTop(BorderStyle.THIN);
		s.setBorderLeft(BorderStyle.THIN);
		s.setBorderRight(BorderStyle.THIN);
		s.setBorderBottom(BorderStyle.THIN);

		Font headerFont = wb.createFont();
		headerFont.setFontName("나눔고딕");
		headerFont.setColor(IndexedColors.BLACK.getIndex());
		headerFont.setBold(true);

		CellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderTop(BorderStyle.THICK);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);

		int rownum = 0;
		int cellnum = 0;

		Row row = null;
		Cell cell = null;

		row = sheet.createRow(rownum++);

		cell = row.createCell(0);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("회원번호");
		
		cell = row.createCell(1);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("가입일");

		cell = row.createCell(2);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("회원ID");

		cell = row.createCell(3);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("전화번호");

		cell = row.createCell(4);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("이메일");

		cell = row.createCell(5);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("추천인");

		cell = row.createCell(6);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("보유번개");

		cell = row.createCell(7);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("보유포인트");

		cell = row.createCell(8);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("예금주");

		cell = row.createCell(9);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("은행명");

		cell = row.createCell(10);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("계좌번호");

		cell = row.createCell(11);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("회원상태");
		
		cell = row.createCell(12);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("하데스 합계");
		
		cell = row.createCell(13);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("포세이돈 합계");
		
		cell = row.createCell(14);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("제우스 합계");
		
		cell = row.createCell(15);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("총 합계");

		
		//데이터 루프 문 시작 가져온 데이터 만큼 반복
		for(HashMap<String, Object> emailFormatInfo : list) {
			System.out.println("emailFormatInfo -> " + emailFormatInfo.toString());
			row = sheet.createRow(rownum++);
			cellnum = 0;

			//유저번호
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("userNo").toString());
			
			//등록일시
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("regDate").toString());

			//유저아이디
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("adminId").toString());

			//유버번호
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("adminHp").toString());

			//유저이메일
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("adminEmail").toString());

			//추천인명
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("recommendName").toString());

			//보유번개
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("nowMoney").toString());

			//보유포인트
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("nowPoint").toString());

			//예금주
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("holderName").toString());

			//은행
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("bank").toString());

			//계좌
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("account").toString());

			//활동상태
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("use_status").toString());
			
			//하데스합계
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("hades").toString());
			
			//포세이돈합계
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("poseidon").toString());
			
			//제우스합계
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("zeus").toString());
			
			//총합계
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(emailFormatInfo.get("total").toString());


		}
		//P.p("sheet :: "+sheet);
		//P.p("row :: "+row);
		
		LocalDate curdate = LocalDate.now();
		
		String fileName = "##########["+curdate+"].xls";
		//fileName = new String(fileName.getBytes("UTF-8"),"ISO-8859-1");

		File file = new File("C:/"+fileName);

		FileOutputStream fos = new FileOutputStream(file);

		try {
			wb.write(fos);
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		System.out.println();
		System.out.println("=========================================");
		
		System.out.println("=========================================");
		System.out.println("excel download 완료");
		System.out.println("=========================================");
		

	}
}
