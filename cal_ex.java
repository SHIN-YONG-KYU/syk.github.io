//정산처리
	@Override
	public ModelAndView calculateHandle(ModelAndView mv, HttpServletRequest request) throws Exception {
		HashMap<String, Object> params = ParamGetter.pGetter(request);
		
		//당일 정산된 품목이 있는지 체크  정산됬으면 true  안됬으면 false 
		if(calculateDao.selectCountCal(params)) {
			return Return.jsonView_msg_result(mv, "금일정산이 이미 완료되었습니다.", false);
		}else {
			//정산처리 purchase_info 업데이트 
			calculateDao.updateCalHandle(params);
			
			//정산처리 purchase_refund_info 의 정산가능내역도 업데이트
			calculateDao.updateRefundHandel(params);
			
			//정산처리 meat_big_order_info 의 정산가능내역 업데이트
			calculateDao.updateMeatBigOrder(params);
			
			//========================== 직통미트를 제외한 판매자 정산로직 ==============================
			
			//정산처리된 seller_uuid의 리스트를 불러옴
			List<HashMap<String, Object>> sellerList = calculateDao.selectSellerList(params);
			log.debug("seller List :: "+sellerList);
			log.debug("seller list size :: "+ sellerList.size());
			
			String msg ="";
			if(sellerList.size() != 0) {
				
				for(int j =0 ; j<sellerList.size(); j++) {
					params.put("SELLER_UUID", sellerList.get(j).get("seller_uuid"));
					
					
					//금일날짜로 정산된 총 리스트를 불러옴(purchase_info 와 purchase_refund_info 의 추가배송비까지)   
					List<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
					list = calculateDao.selectCalHandleListNow(params);
					log.debug("정산완료된 list :"+list);
					
					log.debug(list.size());
					
					
					HashMap<String, Object> params2 = new HashMap<String, Object>();
					
					params2.put("seller_uuid", params.get("SELLER_UUID"));
					params2.put("admin_name", list.get(0).get("admin_name"));
					params2.put("cal_date", list.get(0).get("cal_date"));
					params2.put("account_num", list.get(0).get("account_num"));
					params2.put("account_bank", list.get(0).get("account_bank"));
					params2.put("account_name", list.get(0).get("account_name"));
					params2.put("commission_rate", list.get(0).get("commission_rate"));
					
					
					
					float card_rate = (float) 0.0297; //카드결제 수수료율
					float vacnt_rate = (float) 0.0044; //가상계좌 수수료율
					float account_rate = 0; //계좌이체 수수료율
					float cal_rate = Float.parseFloat(list.get(0).get("commission_rate").toString()); //플랫폼 수수료율
					
					//과세
					int pg_commission =0; //결제대행 수수료
					int cal_commission =0; //플랫폼 수수료
					int total_commission =0; //전체수수료
					int total_delivery_pay = 0; //총배송비
					int total_price = 0; //판매금액
					int total_payment = 0; //과세상품 총매출
					
					//면세
					int pg_commission_y =0; //면세 결제대행 수수료 
					int cal_commission_y =0; //면세 플랫폼 수수료 
					int total_commission_y =0; //전체수수료
					int total_delivery_pay_y = 0; //면세 총배송비
					int total_price_y = 0;//면세 총판매금액
					int total_payment_y = 0; //면세상품 총매출
					
					//정산된 리스트르 반복으로 돌림 개별수수료율 적용
					for(int i=0 ; i<list.size();i++) {
						
						HashMap<String, Object> param = list.get(i);
						
						//판매금액
						int price = Integer.parseInt(param.get("payment_price").toString())*1;
						//배송비
						int delivery_pay = Integer.parseInt(param.get("total_delivery").toString())*1; 
						//합계금액
						int payment = price*1 + delivery_pay*1;
						
						//면세일떄
						if(param.get("taxFree").toString().equals("y")) {
							total_delivery_pay_y += delivery_pay;
							total_price_y += price ;
							
							cal_commission_y += (int)payment*cal_rate; //플랫폼 수수료 추가 
							
							if(param.get("p_method").toString().equals("card")) {
								pg_commission_y += (int)payment*card_rate; //결제대행 카드수수료 추가 
							}else if(param.get("p_method").toString().equals("vacnt")) {
								pg_commission_y += (int)payment*vacnt_rate; //결제대행 가상계좌 수수료 추가 
							}
							
							total_payment_y += delivery_pay*1 + price*1;
							
						}else {//과세일떄
							total_price += price ;
							total_delivery_pay += delivery_pay;
							
							cal_commission += (int)payment*cal_rate; //플랫폼 수수료 추가
							
							if(param.get("p_method").toString().equals("card")) {
								pg_commission += (int)payment*card_rate; //결제대행 카드수수료 추가 
							}else if(param.get("p_method").toString().equals("vacnt")) {
								pg_commission += (int)payment*vacnt_rate; //결제대행 가상계좌 수수료 추가 
							}
							
							total_payment += delivery_pay*1 + price*1; //총매출 추가
							
						}
						
					}
					
					
					//면세상품 정산 등록
					if(total_payment_y > 0) {
						
						//면세상태주기
						params2.put("fee_free", "y");
						
						
						params2.put("pg_commission",pg_commission_y); //대행 수수료
						params2.put("cal_commission",cal_commission_y); //플랫폼 수수료
						params2.put("total_delivery_pay", total_delivery_pay_y); //총배송비
						params2.put("total_price",total_price_y); //총 판매금액
						params2.put("total_payment", total_payment_y); //총 매출
						
						//면세 공급가 부가세 0
						int cal_sup_pay=0;
						int cal_fee = 0;
						params2.put("cal_sup_pay", cal_sup_pay);
						params2.put("cal_fee", cal_fee);
						
						//정산금액
						int cal_payment = (int)(total_payment_y - cal_commission_y - pg_commission_y);
						log.debug("cal_payment:"+cal_payment);
						params2.put("cal_payment", cal_payment);
						
						log.debug("면세 정산 ::"+params2);
						//개별로 정산내역에 insert시킨다.
						calculateDao.insertCalInfo(params2);
						
					}
					
					//과세상품 정산등록
					if(total_payment > 0) {
						//과세상태주기
						params2.put("fee_free", "n");
						
						
						params2.put("pg_commission", pg_commission); //대행 수수료
						params2.put("cal_commission", cal_commission); //플랫폼 수수료
						params2.put("total_delivery_pay", total_delivery_pay); //총배송비
						params2.put("total_price",total_price); //총 판매금액
						params2.put("total_payment", total_payment); //총 매출
						
						//공급가액
						int cal_sup_pay = (int)(total_payment/1.1);
						log.debug("cal_sup_pay :"+cal_sup_pay);
						params2.put("cal_sup_pay", cal_sup_pay);
						
						//부가세
						int cal_fee = (int)(total_payment-cal_sup_pay);
						log.debug("cal_fee:"+cal_fee);
						params2.put("cal_fee", cal_fee);
						
						//정산금액
						int cal_payment = (int)(total_payment - cal_commission - pg_commission);
						log.debug("cal_payment:"+cal_payment);
						params2.put("cal_payment", cal_payment);
						
						log.debug("과세 정산 ::"+params2);
						
						//개별로 정산내역에 insert시킨다.
						calculateDao.insertCalInfo(params2);
					}
					
				}//seller_uuid 별 반복끝
			}
			//==================================  메인 영업점을 제외한 판매자 정산로직) ======================

			
			//================================== 메인 영업점 전체 정산로직 ===================================
			
			//일반(원육,세절육상품) + 추가배송비 + 대량주문 내역 통으로 정산 (면세 과세만 나눈다.)
			List<HashMap<String, Object>> meat_list = calculateDao.selectMeatCalHandleListNow(params);
			
			if(meat_list.size() > 0) {
				
				HashMap<String, Object> params2 = new HashMap<String, Object>();
				
				params2.put("admin_name", meat_list.get(0).get("admin_name"));
				params2.put("cal_group_status","m"); //미트정산 상태값
				params2.put("cal_date", meat_list.get(0).get("cal_date"));
				params2.put("account_num", meat_list.get(0).get("account_num"));
				params2.put("account_bank", meat_list.get(0).get("account_bank"));
				params2.put("account_name", meat_list.get(0).get("account_name"));
				params2.put("commission_rate", meat_list.get(0).get("commission_rate"));
				
				
				
				float card_rate = (float) 0.0297; //카드결제 수수료율
				float vacnt_rate = (float) 0.0044; //가상계좌 수수료율
				float account_rate = 0; //계좌이체 수수료율
				float cal_rate = Float.parseFloat(meat_list.get(0).get("commission_rate").toString()); //플랫폼 수수료율
				
				//과세
				int pg_commission =0; //결제대행 수수료
				int cal_commission =0; //플랫폼 수수료
				int total_commission =0; //전체수수료
				int total_delivery_pay = 0; //총배송비
				int total_price = 0; //판매금액
				int total_payment = 0; //과세상품 총매출
				
				//면세
				int pg_commission_y =0; //면세 결제대행 수수료 
				int cal_commission_y =0; //면세 플랫폼 수수료 
				int total_commission_y =0; //전체수수료
				int total_delivery_pay_y = 0; //면세 총배송비
				int total_price_y = 0;//면세 총판매금액
				int total_payment_y = 0; //면세상품 총매출
				
				//정산된 리스트르 반복으로 돌림 개별수수료율 적용
				for(int i=0 ; i<meat_list.size();i++) {
					
					HashMap<String, Object> param = meat_list.get(i);
					
					//판매금액
					int price = Integer.parseInt(param.get("payment_price").toString())*1;
					//배송비
					int delivery_pay = Integer.parseInt(param.get("total_delivery_pay").toString())*1; 
					//합계금액
					int payment = price*1 + delivery_pay*1;
					
					//면세일떄
					if(param.get("taxFree").toString().equals("y")) {
						total_delivery_pay_y += delivery_pay;
						total_price_y += price ;
						
						cal_commission_y += (int)payment*cal_rate; //플랫폼 수수료 추가 
						
						if(param.get("p_method").toString().equals("card")) {
							pg_commission_y += (int)payment*card_rate; //결제대행 카드수수료 추가 
						}else if(param.get("p_method").toString().equals("vacnt")) {
							pg_commission_y += (int)payment*vacnt_rate; //결제대행 가상계좌 수수료 추가 
						}
						
						total_payment_y += delivery_pay*1 + price*1;
						
					}else {//과세일떄
						total_price += price ;
						total_delivery_pay += delivery_pay;
						
						cal_commission += (int)payment*cal_rate; //플랫폼 수수료 추가
						
						if(param.get("p_method").toString().equals("card")) {
							pg_commission += (int)payment*card_rate; //결제대행 카드수수료 추가 
						}else if(param.get("p_method").toString().equals("vacnt")) {
							pg_commission += (int)payment*vacnt_rate; //결제대행 가상계좌 수수료 추가 
						}
						
						total_payment += delivery_pay*1 + price*1; //총매출 추가
						
					}
					
				}
				
				
				//면세상품 정산 등록
				if(total_payment_y > 0) {
					
					//면세상태주기
					params2.put("fee_free", "y");
					
					
					params2.put("pg_commission",pg_commission_y); //대행 수수료
					params2.put("cal_commission",cal_commission_y); //플랫폼 수수료
					params2.put("total_delivery_pay", total_delivery_pay_y); //총배송비
					params2.put("total_price",total_price_y); //총 판매금액
					params2.put("total_payment", total_payment_y); //총 매출
					
					//면세 공급가 부가세 0
					int cal_sup_pay=0;
					int cal_fee = 0;
					params2.put("cal_sup_pay", cal_sup_pay);
					params2.put("cal_fee", cal_fee);
					
					//정산금액
					int cal_payment = (int)(total_payment_y - cal_commission_y - pg_commission_y);
					log.debug("cal_payment:"+cal_payment);
					params2.put("cal_payment", cal_payment);
					
					log.debug("면세 정산 ::"+params2);
					//개별로 정산내역에 insert시킨다.
					calculateDao.insertCalInfo(params2);
					
				}
				
				//과세상품 정산등록
				if(total_payment > 0) {
					//과세상태주기
					params2.put("fee_free", "n");
			
					params2.put("pg_commission", pg_commission); //대행 수수료
					params2.put("cal_commission", cal_commission); //플랫폼 수수료
					params2.put("total_delivery_pay", total_delivery_pay); //총배송비
					params2.put("total_price",total_price); //총 판매금액
					params2.put("total_payment", total_payment); //총 매출
					
					//공급가액
					int cal_sup_pay = (int)(total_payment/1.1);
					log.debug("cal_sup_pay :"+cal_sup_pay);
					params2.put("cal_sup_pay", cal_sup_pay);
					
					//부가세
					int cal_fee = (int)(total_payment-cal_sup_pay);
					log.debug("cal_fee:"+cal_fee);
					params2.put("cal_fee", cal_fee);
					
					//정산금액
					int cal_payment = (int)(total_payment - cal_commission - pg_commission);
					log.debug("cal_payment:"+cal_payment);
					params2.put("cal_payment", cal_payment);
					
					log.debug("과세 정산 ::"+params2);
					
					//개별로 정산내역에 insert시킨다.
					calculateDao.insertCalInfo(params2);
				}
				
				
			}
			//================================== 정산로직 끝 =================================================
			
			
			if(sellerList.size() == 0 && meat_list.size() == 0) {
				msg ="정산가능내역이 없습니다.";
			}else {
				msg ="정산이 완료되었습니다.";
			}
			
			
			return Return.jsonView_msg_result(mv, msg, true);
		}
		
		
	}
