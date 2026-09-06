package org.barcelonajug.superherobattlearena.adapter.in.web.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.barcelonajug.superherobattlearena.adapter.in.web.HeroController;
import org.barcelonajug.superherobattlearena.adapter.in.web.dto.HeroDto;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class HeroModelAssembler
    implements RepresentationModelAssembler<Hero, EntityModel<HeroDto>> {

  @Override
  public EntityModel<HeroDto> toModel(Hero hero) {
    return EntityModel.of(
        HeroDto.from(hero),
        linkTo(methodOn(HeroController.class).getHeroById(hero.id())).withSelfRel(),
        linkTo(methodOn(HeroController.class).getAllHeroes(0, 20)).withRel("heroes"));
  }
}
