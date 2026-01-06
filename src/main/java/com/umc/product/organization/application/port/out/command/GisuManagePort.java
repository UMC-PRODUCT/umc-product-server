package com.umc.product.organization.application.port.out.command;


import com.umc.product.organization.domain.Gisu;

public interface GisuManagePort {

    Gisu save(Gisu gisu);

    void delete(Gisu gisu);
}
