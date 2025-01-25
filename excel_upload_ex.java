//단가리스트 업로드
	@Transactional
	@Override
	public ModelAndView priceListUpload(ModelAndView mv, MultipartHttpServletRequest req) throws Exception {
		HashMap<String, Object> params = ParamGetter.pGetter(req);
		log.debug("params :: "+params);
		
		boolean verify = true;
		
		//내용닯을 list 
		List<HashMap<String, Object>> excel_list =new ArrayList<HashMap<String,Object>>();
		
		//파일읽어들이기
		try {
			MultipartFile file = null;
			Iterator<String> iterator = req.getFileNames();
			log.debug("파일 존재유뮤 :: "+iterator.hasNext()); 
			
			//파일명
			String fileName ="";
			
			//멀티파트에 담긴내용들 반복돌림
			while(iterator.hasNext()) {
				
				log.debug("itertor while문 가동 ");
				fileName =  iterator.next();
				log.debug("file_name :: "+fileName);
				
				
				//파일이름이 excel_data인경우만 file에 담기
				if(fileName.equals("FILE_EXCEL")) {
					file = req.getFile(fileName);
				}
				
			}
			
			
			//엑셀파일열기( 엑셀버전 2007 이상만 가능 )
			OPCPackage opcPackage = OPCPackage.open(file.getInputStream());
			XSSFWorkbook wb = new XSSFWorkbook(opcPackage);
			
			int sheetNum = wb.getNumberOfSheets(); // sheet수
			log.debug("sheet 수 :: "+sheetNum);
			
			
			//시트개수반복
			for(int i=0 ;i<sheetNum; i++) {
				XSSFSheet sheet = wb.getSheetAt(i);//시트를 순서대로 가져옴
				
				int rows = sheet.getPhysicalNumberOfRows();//시트아래 있는 행의 개수
				
				//행의개수 만큼 반복
				for(int j=0; j<rows;j++) {
					XSSFRow row = sheet.getRow(j);//각행마다 전체데이터를 받아옴
					int cells = row.getPhysicalNumberOfCells();//행안에있는 셀의개수
					log.debug(j + "번 행의 셀의개수 ::"+cells );
					
					HashMap<String, Object> paramMap = new HashMap<String, Object>(); //데이터저장용 map
					
					for(int k=0;k < cells;k++) {
						XSSFCell cell = row.getCell(k); //각셀의 데이터를 순서대로 받아옴
						String value = "";
						
						if(cell == null) {
							continue;
						}
						else {
							
							switch (cell.getCellType()) {
							case FORMULA:
								value = cell.getCellFormula();
								break;
							case NUMERIC:
								value = cell.getNumericCellValue() + "";
								break;
							case STRING:
								value = cell.getStringCellValue() + "";
								break;
							case BLANK:
								value = cell.getBooleanCellValue() +"";
								break;
							case ERROR:
								value = cell.getErrorCellString() + "";
								break;
							}
							
							
						}
						
						
						//log.debug("value : "+value+", j :"+j + ", k : "+ k);
						
						switch (k) {
							case 1:
								paramMap.put("price_uuid", value);
							break;
							case 2:
								paramMap.put("category", value);
								break;
							case 3:
								paramMap.put("s_category", value);
								break;
							case 4:
								paramMap.put("ss_category", value);
								break;
							case 5:
								paramMap.put("brand_name", value);
								break;
							case 6:
								paramMap.put("use_name", value);
								break;
							case 7:
								paramMap.put("prod_status", value);
								break;
							case 8:
								paramMap.put("meat_price", value);
								break;
							case 9:
								paramMap.put("bulk_price", value);
								break;
							case 10:
								paramMap.put("use_s_date", value);
								break;

							default:
								break;
						}
					}//k반복
					log.debug(j+"번 행의 전체 값::"+paramMap.toString());
					
					//excel_list에 데이터 차곡차곡 쌓음
					excel_list.add(paramMap);
				}//j반복 row
			}//i반복 sheet
			
			//log.debug("excel_list :: "+excel_list);
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			verify = false;
		}
		
		
		//예외 상황으로 걸러지냐 아니냐 판별
		if(verify) {
			excel_list.remove(0); //cell 헤드부분 삭제
			log.debug("excel list :: "+excel_list);
			
			//유효성검사 상태
			boolean result = true;
			String msg = "";
			
			
			
			//유효성검사
			for(int i =0; i< excel_list.size();i++) {
				HashMap<String, Object> info = excel_list.get(i);
				
				//uuid 빠진값
				if(info.get("price_uuid").equals("false") || info.get("price_uuid").equals("null") ) {
					result = false;
					msg = "uuid 에 없는 값들이 있습니다. 다시 업로드해주세요.";
					break;
				}else {
					if(!info.get("price_uuid").toString().chars().allMatch(Character::isDigit)) {
						result = false;
						msg = "uuid 는 변경할수없습니다. 다시 업로드해주세요.";
						break;
					}
				}
				
				//판매단가 빠진값
				if(info.get("meat_price").equals("false") || info.get("meat_price").equals("null")) {
					result = false;
					msg = "판매단가에 없는 값들이 있습니다. 다시 업로드해주세요.";
					break;
				}
				
				//판매단가 
				//log.debug("판매단가 숫자판별 :: "+info.get("meat_price").toString().chars().allMatch(Character::isDigit));
				log.debug(i+"번 판매단가 숫자판별 :: "+info.get("meat_price").toString().matches("[-+]?\\d*\\.?\\d+"));
				if(!info.get("meat_price").toString().matches("[-+]?\\d*\\.?\\d+")) {
					result = false;
					msg = "판매단가는 숫자만 허용됩니다. 다시 업로드해주세요.";
					break;
				}
				
				//주문서단가 값이 있을때만
				if( !info.get("bulk_price").equals("false") && ! info.get("bulk_price").equals("null"))  {
					
					log.debug(i+"번 주문서단가 숫자판별 :: "+info.get("meat_price").toString().matches("[-+]?\\d*\\.?\\d+"));
					if(!info.get("bulk_price").toString().matches("[-+]?\\d*\\.?\\d+")) {
						result = false;
						msg = "주문서단가는 숫자만 허용됩니다. 다시 업로드해주세요.";
						break;
					}
				}
				
				
			}
			
			//날짜비교 적용일자와 현재일자
			Date date = new Date(); //현재일자
			String use_s_date = params.get("USE_S_DATE").toString();
			String today = params.get("TODAY").toString();
			boolean eq ;
			
			if(use_s_date.equals(today)) {
				eq = true; //같음
			}else {
				eq = false; //다름
			}
			
			log.debug("적용일자와 현재일자가 같은지 :: "+eq);
			
			
			//유효성검사 후에
			if(result) {
				
				//파일 업데이트
				for(int i=0 ;i<excel_list.size();i++) {
					HashMap<String, Object> param = new HashMap<String, Object>(); // 개별해쉬맵
					
					//excel_list의 적용일자 전체변경
					//적용일자와 오늘이 같을떄
					if(eq) {
						param.put("USE_S_DATE", date); //현재날짜
						//일반값넣음
						param.put("MEAT_PRICE", (int)Double.parseDouble( excel_list.get(i).get("meat_price").toString()) +""); 
						
						//bulk_price가 존재할때만 값입력
						if(!excel_list.get(i).get("bulk_price").equals("false") && !excel_list.get(i).get("bulk_price").equals("null")) {
							param.put("BULK_PRICE", (int)Double.parseDouble( excel_list.get(i).get("bulk_price").toString()) + ""); 
						}else {
							//bulk_price가 존재하지 않을떄
							param.put("BULK_PRICE","x");
						}
						
						//x로 상태값준다.null 주기위해
						param.put("CHANGE_MEAT_PRICE","x");
						param.put("CHANGE_BULK_PRICE","x");
						
					}
					//적용일자와 오늘이 틀릴떄
					else {
						param.put("CHA_USE_S_DATE", use_s_date); //적용일자
						//change 값넣음
						param.put("CHANGE_MEAT_PRICE", (int)Double.parseDouble(excel_list.get(i).get("meat_price").toString()) +""); 
						
						if(!excel_list.get(i).get("bulk_price").equals("false")  && !excel_list.get(i).get("bulk_price").equals("null")) {
							param.put("CHANGE_BULK_PRICE", (int)Double.parseDouble(excel_list.get(i).get("bulk_price").toString()) +""); 
						}else {
							param.put("CHANGE_BULK_PRICE","x");
						}
					}
					
					param.put("ADMIN_UUID", params.get("ADMIN_UUID")); //admin_uuid
					param.put("PRICE_UUID", excel_list.get(i).get("price_uuid")); // price_uuid
					
					
					//업데이트도중 실수하는거 체크
					try {
						// meat_price_info 에 update시켜준다
						branchDao.updatePriceInfo(param);
						
					} catch (Exception e) {
						log.debug(i+"번 업데이트 도중 오류");
						e.printStackTrace();
					}
				}
				
				return Return.jsonView_msg_result(mv, "단가등록이 완료되었습니다.", true);
			}else {
				return Return.jsonView_msg_result(mv, msg, false);
			}
			
			
		}else {
			
			return Return.jsonView_msg_result(mv, "업로드도중 에러가 발생했습니다.<br>관리자에게 문의해주세요.", false);
		}
		
	}
