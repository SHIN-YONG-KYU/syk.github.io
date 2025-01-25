CREATE */ /*!50017 DEFINER = '###########' */ /*!50003 TRIGGER `option_info_insert` AFTER INSERT ON `prod_option_info` FOR EACH ROW BEGIN

    -- 상품의 수량 추가 등록시
		-- sell_type이 주문생산이 아닐때만 재고내역에 추가
		if new.sell_type != 'y' then 
		
	
			if new.group_status = 'm' then
				
				/* meat_sort_history 에 입고 잡는다. */	
				insert into meat_sort_history
					(admin_uuid
					,option_uuid
					,category_uuid
					,s_category_uuid
					,ss_category_uuid
					,brand_uuid
					,use_uuid
					,prod_status
					,input_sort
					,reg_date
					,history_status
					)
				select 
					admin_uuid
					,new.option_uuid
					,category_uuid 
					,s_category_uuid
					,ss_category_uuid
					,brand_uuid
					,use_uuid
					,prod_status
					,( new.meat_kg * new.option_stock)
					,NOW()
					,'IN' 
				
				from prod_info
				WHERE prod_uuid = new.prod_uuid ;
				
				
				/* prod_stock_total 에 입고 잡는다. 수량한개라  */	
				INSERT INTO prod_stock_total
					(option_uuid
					,prod_uuid
					,input_stock
					,reg_date
					,stock_status
					)
				VALUES(
					new.option_uuid
					,new.prod_uuid
					,new.option_stock
					,NOW()
					,'IN'
				);
				
				
			
			end if;
			
			
			if new.group_status = 'n' then
				/* prod_stock_total 에 입고 잡는다. */	
				INSERT INTO prod_stock_total
					(option_uuid
					,prod_uuid
					,input_stock
					,reg_date
					,stock_status
					)
				values(
					new.option_uuid
					,new.prod_uuid
					,new.option_stock
					,NOW()
					,'IN'
				);
				
			end if; -- //단일상품등록일떄 
			
			-- 세절상품 등록일떄
			if new.group_status = 'ms' then
				-- prod_stock_total 에 insert 시킨다
				INSERT INTO prod_stock_total
					(option_uuid
					,prod_uuid
					,input_stock
					,reg_date
					,stock_status
					)
				VALUES(
					new.option_uuid
					,new.prod_uuid
					,new.option_stock
					,NOW()
					,'IN'
				);
				
				
			end if;
			
			-- 공동구매 재고 등록일떄
			IF new.group_status = 'y' THEN
				-- prod_stock_total 에 insert 시킨다
				INSERT INTO prod_stock_total
					(option_uuid
					,prod_uuid
					,input_stock
					,reg_date
					,stock_status
					)
				VALUES(
					new.option_uuid
					,new.prod_uuid
					,new.option_stock
					,NOW()
					,'IN'
				);
				
				
			END IF;
			
			-- 이벤트상품  재고 등록일떄
			IF new.group_status = 'e' THEN
				-- prod_stock_total 에 insert 시킨다
				INSERT INTO prod_stock_total
					(option_uuid
					,prod_uuid
					,input_stock
					,reg_date
					,stock_status
					)
				VALUES(
					new.option_uuid
					,new.prod_uuid
					,new.option_stock
					,NOW()
					,'IN'
				);
				
				
			END IF;
			
			-- 거래처 등록상품  재고 등록일떄
			IF new.group_status = 'market' THEN
				-- prod_stock_total 에 insert 시킨다
				INSERT INTO prod_stock_total
					(option_uuid
					,prod_uuid
					,input_stock
					,reg_date
					,stock_status
					)
				VALUES(
					new.option_uuid
					,new.prod_uuid
					,new.option_stock
					,NOW()
					,'IN'
				);
				
				
			END IF;
			
			-- 기타상품  재고 등록일떄
			IF new.group_status = 'other' THEN
				-- prod_stock_total 에 insert 시킨다
				INSERT INTO prod_stock_total
					(option_uuid
					,prod_uuid
					,input_stock
					,reg_date
					,stock_status
					)
				VALUES(
					new.option_uuid
					,new.prod_uuid
					,new.option_stock
					,NOW()
					,'IN'
				);
				
				
			END IF;
			
			
			
		END IF; -- // sell_type 이 주문생산이 아닐떄
	
    END */$$


DELIMITER ;
