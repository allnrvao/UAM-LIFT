package ni.edu.uam.UAM_LIFT.repositories;


import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface RepoGeneral<T,ID> extends Repository<T,ID> {

        boolean Add(T t);
        boolean Update(T t);
        boolean Delete(T t);
        String findByUnique(String unique);
}