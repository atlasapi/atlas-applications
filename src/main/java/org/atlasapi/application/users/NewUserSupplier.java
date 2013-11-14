package org.atlasapi.application.users;

import org.atlasapi.application.users.v3.User;

import com.google.common.base.Supplier;
import com.metabroadcast.common.ids.IdGenerator;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;

public class NewUserSupplier implements Supplier<User> {

    private final IdGenerator idGenerator;
    private SubstitutionTableNumberCodec codec;

    public NewUserSupplier(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        this.codec = new SubstitutionTableNumberCodec();
    }
    
    @Override
    public User get() {
        return new User(codec.decode(idGenerator.generate()).longValue());
    }

}
