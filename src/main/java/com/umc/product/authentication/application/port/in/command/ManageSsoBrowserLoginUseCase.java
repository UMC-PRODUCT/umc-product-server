package com.umc.product.authentication.application.port.in.command;

import com.umc.product.authentication.application.port.in.command.dto.LoginSsoBrowserByEmailCommand;
import com.umc.product.authentication.application.port.in.dto.SsoBrowserLoginInfo;

public interface ManageSsoBrowserLoginUseCase {

    SsoBrowserLoginInfo loginByEmail(LoginSsoBrowserByEmailCommand command);
}
