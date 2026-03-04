package org.barcelonajug.superherobattlearena.adapter.in.web.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.barcelonajug.superherobattlearena.adapter.in.web.HeroController;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class HeroModelAssembler implements RepresentationModelAssembler<Hero, EntityModel<Hero>> {

  @Override
  public EntityModel<Hero> toModel(Hero hero) {
    return EntityModel.of(
        hero,
        linkTo(methodOn(HeroController.class).getHeroById(hero.id())).withSelfRel(),
        linkTo(methodOn(HeroController.class).getAllHeroes(0, 20)).withRel("heroes"));
  }
}
