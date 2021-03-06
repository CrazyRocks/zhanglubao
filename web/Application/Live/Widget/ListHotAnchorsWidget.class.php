<?php
namespace Live\Widget;

use Think\Action;

/**
 * 分类widget
 * 用于动态调用分类信息
 */
class ListHotAnchorsWidget extends Action
{
    public function lists($id = '', $order = 'sort desc')
    {
        //$anchors = S('live_list_na_'.$id);
        if (empty($anchors)) {
        	$map['status']=1;
        	if ($id) {
        		$map['game_id']=$id;
        	}
            $anchors = D('Live')->getLivesInfo($map,15,'id desc');
            S('live_list_na_'.$id, $anchors, 3000);
        }
        $this->assign('anchors', $anchors);
        $this->display('Widget/List/anchors');
    }
}