

package jp.co.internous.peace.controller;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import jp.co.internous.peace.model.domain.TblCart;
import jp.co.internous.peace.model.domain.dto.CartDto;
import jp.co.internous.peace.model.form.CartForm;
import jp.co.internous.peace.model.mapper.TblCartMapper;
import jp.co.internous.peace.model.session.LoginSession;

//Controllerとして機能する
@Controller
//指定のurlでcontroller起動
@RequestMapping("/peace/cart")
public class CartController {
	
	//TblCartMapperクラスのインスタンスを代入
	@Autowired
	TblCartMapper cartMapper;
	
	//LoginSessionクラスのインスタンスを代入
	@Autowired
	LoginSession loginSession;
	
	//Gsonを利用する為インスタンスを生成
	private Gson gson = new Gson();
	
	@RequestMapping("/")
	public String index(Model m) {
		//ユーザーがログインしていればユーザーIdを、そうでなければ仮ユーザーIdをuserIdに代入する
		int userId = loginSession.isLogined() ? loginSession.getUserId() : loginSession.getTmpUserId();

		//　該当するuserIdのカート情報(商品の種類、個数、画像情報等)をsqlのselectで取得して
		// データ移動用のリストクラス変数に追加してその情報をviewに渡す
		List<CartDto> carts = cartMapper.findByUserId(userId);
		m.addAttribute("loginSession",loginSession);
		m.addAttribute("carts",carts);
		return "cart";
	}
	
	//カート情報の中身をこのメソッドで受け取る
	@RequestMapping("/add")
	//CartFormにviewから受け取った情報が渡される
	public String addCart(CartForm f, Model m) {
		
		//ユーザーがログインしていればユーザーIdを、そうでなければ仮ユーザーIdをuserIdに代入する
		int userId = loginSession.isLogined() ? loginSession.getUserId() : loginSession.getTmpUserId();
		
		//セッションで取得したユーザーIdをカートフォームのuserIdに代入
		f.setUserId(userId);
		
		//　カートテーブル(db対応)にフォームとユーザーの情報を代入
		TblCart cart = new TblCart(f);
		int result = 0;
		// tbl_cartに対応するuserIdと製品(productId)の情報がひとつ以上あれば該当する個数を更新
		// そうでなければ新しくレコードを追加する
		if (cartMapper.findCountByUserIdAndProuductId(userId, f.getProductId()) > 0) {
			result = cartMapper.update(cart);
		} else {
			result = cartMapper.insert(cart);
		}
		//製品情報がひとつ以上あれば
		if (result > 0) {
			//　該当するuserIdのカート情報(商品の種類、個数、画像情報等)をsqlのselectで取得して
			// データ移動用のリストクラス変数に追加してその情報をviewに渡す
			List<CartDto> carts = cartMapper.findByUserId(userId);
			m.addAttribute("loginSession",loginSession);
			m.addAttribute("carts",carts);
		}		
		return "cart";
	}
	//型変換の際にでる警告を無効にする。
	//今回の場合はjsonオブジェクトをMapに変換するケース
	@SuppressWarnings("unchecked")
	@RequestMapping("/delete")
	//returnの内容自体を返す
	@ResponseBody
	//json形式のデータを受け取る
	public boolean deleteCart(@RequestBody String checkedIdList) {
		int result = 0;
		//Map型変数にjsonを代入(キー:checkedIdList,値:チェックされたcartのidリスト)
		Map<String, List<String>> map = gson.fromJson(checkedIdList, Map.class);
		//チェックされたcartのidリストを変数に代入
		List<String> checkedIds = map.get("checkedIdList");
		//idリストを利用してtbl_cartの該当部分を削除
		for (String id : checkedIds) {
			result += cartMapper.deleteById(Integer.parseInt(id));
		}
		
		return result > 0;
	}
	
}

