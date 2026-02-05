package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.Gisu;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadGisuPort {

    Gisu findActiveGisu();

    Gisu findById(Long gisuId);

    List<Gisu> findAll();

<<<<<<< HEAD
    Page<Gisu> findAll(Pageable pageable);

=======
>>>>>>> 5447cb8f1af6a362cee69dfbc502fd0ba238cd48
    boolean existsByGeneration(Long generation);
}
