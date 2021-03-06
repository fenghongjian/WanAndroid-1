package per.goweii.wanandroid.module.knowledge.view;

import per.goweii.basic.core.base.BaseView;
import per.goweii.wanandroid.module.knowledge.model.KnowledgeArticleBean;

/**
 * @author CuiZhen
 * @date 2019/5/12
 * QQ: 302833254
 * E-mail: goweii@163.com
 * GitHub: https://github.com/goweii
 */
public interface KnowledgeArticleView extends BaseView {
    void getKnowledgeArticleListSuccess(int code, KnowledgeArticleBean data);
    void getKnowledgeArticleListFail(int code, String msg);
}
