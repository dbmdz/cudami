/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.digitalcollections.cudami.server.backend.impl.jpa.repository;

import de.digitalcollections.cudami.server.backend.impl.jpa.entity.UserImplJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * To execute Querydsl predicates we simply let our repository extend QueryDslPredicateExecutor.
 *
 * @author ralf
 */
public interface UserRepositoryJpa extends JpaRepository<UserImplJpa, Long>, QueryDslPredicateExecutor {

    public UserImplJpa findByEmail(String email);

}
