//단가내역 리스트 엑셀다운로드
	@Override
	public void priceListExcel(ModelAndView mv, HttpServletRequest req,HttpServletResponse res) throws Exception {
		HashMap<String, Object> params = ParamGetter.pGetter(req);

		//현재 날짜데이터
		Date now = new Date();
		SimpleDateFormat now1 = new SimpleDateFormat("yy.MM.dd");
		String nowTime = now1.format(now);
		log.debug("날짜 형식 :"+nowTime);
		
	
		HttpSession session = req.getSession();
		HashMap<String, Object> adminInfo = (HashMap<String, Object>) session.getAttribute("adminInfo");
		params.put("GRADE_NUM", adminInfo.get("GRADE_NUM"));
		params.put("ADMIN_UUID", adminInfo.get("ADMIN_UUID"));

		
		//단가리스트
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
		list =branchDao.selectPriceList(params); 
		log.debug("list : "+list);
		
		XSSFWorkbook wb = new XSSFWorkbook();
		log.debug("list size -> "+  list.size());
		XSSFSheet sheet = wb.createSheet("단가내역");
		//sheet.autoSizeColumn(0);
		
		//사이즈 default 설정
		//sheet.setDefaultColumnWidth(6000);
		
		//사이즈 조정
		for(int i =0 ; i<= 10;i++) {
			sheet.autoSizeColumn(i);
			sheet.setColumnWidth(i, (sheet.getColumnWidth(i))+(short)2000);
		}
		
		//cell 스타일 
		CellStyle s = wb.createCellStyle();
		s.setAlignment(HorizontalAlignment.CENTER);
		s.setVerticalAlignment(VerticalAlignment.CENTER);
		s.setBorderTop(BorderStyle.THIN);
		s.setBorderLeft(BorderStyle.THIN);
		s.setBorderRight(BorderStyle.THIN);
		s.setBorderBottom(BorderStyle.THIN);
		
		//cell 2 에 적용할 폰트
		Font cell2 = wb.createFont();
		cell2.setFontName("나눔고딕");
		cell2.setColor(IndexedColors.BLACK.getIndex());
		cell2.setBold(true);
		
		
		//cell 2번쨰 스타일
		CellStyle s2 = wb.createCellStyle();
		s2.setFont(cell2);
		s2.setAlignment(HorizontalAlignment.CENTER);
		s2.setVerticalAlignment(VerticalAlignment.CENTER);
		s2.setBorderTop(BorderStyle.THIN); //테두리
		s2.setBorderLeft(BorderStyle.THIN); //테두리
		s2.setBorderRight(BorderStyle.THIN); //테두리
		s2.setBorderBottom(BorderStyle.THIN); //테두리
		s2.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex()); //배경색
		s2.setFillPattern(FillPatternType.SOLID_FOREGROUND); //채우기적용
		
		
		
		//headrStyle에 적용할 폰트 
		Font headerFont = wb.createFont();
		headerFont.setFontName("나눔고딕");
		headerFont.setColor(IndexedColors.BLACK.getIndex());
		headerFont.setBold(true);
		
		//headerStyle 정의
		CellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderTop(BorderStyle.THICK);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); //배경색
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); //채우기적용
		
		int rownum = 0;
		int cellnum = 0;

		Row row = null;
	    Cell cell = null;
	    
	    row = sheet.createRow(rownum++);
	    
	    cell = row.createCell(0);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("번호(변경불가)");
		
		cell = row.createCell(1);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("uuid(변경불가)");
		
		cell = row.createCell(2);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("대분류(변경불가)");
		
		cell = row.createCell(3);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("중분류(변경불가)");
		
		cell = row.createCell(4);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("소분류(변경불가)");
		
		cell = row.createCell(5);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("브랜드(변경불가)");
		
		cell = row.createCell(6);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("용도(변경불가)");
		
		cell = row.createCell(7);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("상태(변경불가)");
		
		cell = row.createCell(8);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("판매단가");
		
		cell = row.createCell(9);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("주문서단가");
		
		cell = row.createCell(10);
		cell.setCellStyle(headerStyle);
		cell.setCellValue("적용일자(변경불가)");
		
		
		
		//번호
		int num = 1;
		
		//데이터 루프 문 시작 가져온 데이터 만큼 반복
		for(HashMap<String, Object> price_list : list) {
			System.out.println("price_list -> " + price_list.toString());
			row = sheet.createRow(rownum++);
			cellnum = 0;
			
			
			
			//번호
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(num);
			num++;
			
			//고유번호
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(price_list.get("price_uuid").toString());
			
			//대분류
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(price_list.get("category").toString());
			
			//중분류
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(price_list.get("s_category").toString());
			
			//소분류
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(price_list.get("ss_category").toString());
			
			//브랜드
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(price_list.get("brand_name").toString());
			
			//용도
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(price_list.get("use_name").toString());
			
			//상태
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(price_list.get("prod_status").toString());
			
			//판매단가
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s2);
			cell.setCellValue(price_list.get("meat_price").toString());
			
			//주문서단가
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s2);
			cell.setCellValue(price_list.get("bulk_price") == null ? "":price_list.get("bulk_price").toString());
			
			//적용일자
			cell = row.createCell(cellnum++);
			cell.setCellStyle(s);
			cell.setCellValue(price_list.get("use_s_date").toString());
			

		}
		//P.p("sheet :: "+sheet);
		//P.p("row :: "+row);
		System.out.println("email info file make end");
		
		String fileName = "단가내역["+ nowTime+"].xls";
		log.debug("파일명 : "+fileName);
		fileName = new String(fileName.getBytes("UTF-8"),"ISO-8859-1");
		//엑셀 확장자
		res.setContentType("application/vnd.ms-excel");
		// 엑셀 파일명 설정
		res.setHeader("Content-Disposition", "attachment;filename=" + fileName);
		
		try {
			wb.write(res.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
