<?php
namespace Admin\Model;
use Think\Model;

/**
 * 分类模型
 * @author Rocks
 */
class GameModel extends Model{

	protected $_validate = array(
	array('name', 'require', '标识不能为空', self::EXISTS_VALIDATE, 'regex', self::MODEL_BOTH),
	array('name', '', '标识已经存在', self::VALUE_VALIDATE, 'unique', self::MODEL_BOTH),
	array('title', 'require', '名称不能为空', self::MUST_VALIDATE , 'regex', self::MODEL_BOTH),
	array('meta_title', '1,50', '网页标题不能超过50个字符', self::VALUE_VALIDATE , 'length', self::MODEL_BOTH),
	array('keywords', '1,255', '网页关键字不能超过255个字符', self::VALUE_VALIDATE , 'length', self::MODEL_BOTH),
	array('meta_title', '1,255', '网页描述不能超过255个字符', self::VALUE_VALIDATE , 'length', self::MODEL_BOTH),
	);

	protected $_auto = array(
	array('type', 'arr2str', self::MODEL_BOTH, 'function'),
	array('type', null, self::MODEL_BOTH, 'ignore'),
	array('reply_model', 'arr2str', self::MODEL_BOTH, 'function'),
	array('reply_model', null, self::MODEL_BOTH, 'ignore'),
	array('extend', 'json_encode', self::MODEL_BOTH, 'function'),
	array('extend', null, self::MODEL_BOTH, 'ignore'),
	array('create_time', NOW_TIME, self::MODEL_INSERT),
	array('update_time', NOW_TIME, self::MODEL_BOTH),
	array('status', '1', self::MODEL_BOTH),
	);


	/**
	 * 获取分类详细信息
	 * @param  milit   $id 分类ID或标识
	 * @param  boolean $field 查询字段
	 * @return array     分类信息
	 * @author Rocks
	 */
	public function info($id, $field = true){
		/* 获取分类信息 */
		$map = array();
		if(is_numeric($id)){ //通过ID查询
			$map['id'] = $id;
		} else { //通过标识查询
			$map['name'] = $id;
		}
		return $this->field($field)->where($map)->find();
	}

	/**
	 * 获取分类树，指定分类则返回指定分类极其子分类，不指定则返回所有分类树
	 * @param  integer $id    分类ID
	 * @param  boolean $field 查询字段
	 * @return array          分类树
	 * @author Rocks
	 */
	public function getTree($id = 0, $field = true){
		/* 获取当前分类信息 */
		if($id){
			$info = $this->info($id);
			$id   = $info['id'];
		}

		/* 获取所有分类 */
		$map  = array('status' => array('gt', -1));
		$list = $this->field($field)->where($map)->order('sort')->select();
		$list = list_to_tree($list, $pk = 'id', $pid = 'pid', $child = '_', $root = $id);

		/* 获取返回数据 */
		if(isset($info)){ //指定分类则返回当前分类极其子分类
			$info['_'] = $list;
		} else { //否则返回所有分类
			$info = $list;
		}

		return $info;
	}

	/**
	 * 获取指定分类的同级分类
	 * @param  integer $id    分类ID
	 * @param  boolean $field 查询字段
	 * @return array
	 * @author Rocks
	 */
	public function getSameLevel($id, $field = true){
		$info = $this->info($id, 'pid');
		$map = array('pid' => $info['pid'], 'status' => 1);
		return $this->field($field)->where($map)->order('sort')->select();
	}

	/**
	 * 更新分类信息
	 * @return boolean 更新状态
	 */
	public function update(){
		$data = $this->create();
		if(!$data){ //数据对象创建错误
			return false;
		}

		/* 添加或更新数据 */
		if(empty($data['id'])){
			$res = $this->add();
		}else{
			$res = $this->save();
		}

		//更新分类缓存
		S('sys_game_list', null);
		$this->cleanCache(array($data['id']));
		$this->cleanCachebyName(array($data['name']));
		//记录行为
		action_log('update_game', 'game', $data['id'] ? $data['id'] : $res, UID);

		return $res;
	}


	/**
	 * 清除指定Game数据
	 *
	 */
	public function cleanCachebyName($names) {
		if (empty ( $name )) {
			return false;
		}
		! is_array ( $names ) && $names = explode ( ',', $names );
		foreach ( $names as $name ) {
			static_cache ( 'game_info_name_' . $name, false );
			$keys =S('game_info_name_'.$name);
			foreach ($keys as $k){
				S($k,null);
			}
			S('game_info_name_'.$id,null);
		}

		return true;
	}


	/**
	 * 清除指定Game数据
	 *
	 */
	public function cleanCache($ids) {
		if (empty ( $ids )) {
			return false;
		}
		! is_array ( $ids ) && $ids = explode ( ',', $ids );
		foreach ( $ids as $id ) {
			static_cache ( 'game_info_' . $id, false );
			$keys =S('game_info_'.$id);
			foreach ($keys as $k){
				S($k,null);
			}
			S('game_info_'.$id,null);
		}

		return true;
	}

	/**
	 * 查询后解析扩展信息
	 * @param  array $data 分类数据
	 * @author Rocks
	 */
	protected function _after_find(&$data, $options){
		/* 分割模型 */
		if(!empty($data['model'])){
			$data['model'] = explode(',', $data['model']);
		}

		/* 分割文档类型 */
		if(!empty($data['type'])){
			$data['type'] = explode(',', $data['type']);
		}

		/* 分割模型 */
		if(!empty($data['reply_model'])){
			$data['reply_model'] = explode(',', $data['reply_model']);
		}

		/* 分割文档类型 */
		if(!empty($data['reply_type'])){
			$data['reply_type'] = explode(',', $data['reply_type']);
		}

		/* 还原扩展数据 */
		if(!empty($data['extend'])){
			$data['extend'] = json_decode($data['extend'], true);
		}
	}
	
	
	 
}
