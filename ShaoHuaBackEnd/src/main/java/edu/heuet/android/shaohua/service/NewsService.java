package edu.heuet.android.shaohua.service;

import edu.heuet.android.shaohua.dataobject.NewsDO;

import java.util.List;

public interface NewsService {
    NewsDO selectDetailById(long id);

    List<NewsDO> selectDetailByTitle(String title);

    List<NewsDO> selectAll();
}
